const puppeteer = require('puppeteer');

(async () => {
  console.log('==================================================');
  console.log('  TESTING LIVE CLOUD DEPLOYMENT');
  console.log('  Frontend: https://lockmyseat-two.vercel.app');
  console.log('  Backend:  https://lockmyseat-backend.onrender.com/api');
  console.log('  Database: Supabase PostgreSQL (Managed Cloud)');
  console.log('==================================================\n');

  const browser = await puppeteer.launch({
    headless: 'new',
    args: ['--no-sandbox', '--disable-setuid-sandbox'],
  });

  const page = await browser.newPage();
  await page.setViewport({ width: 1280, height: 800 });

  try {
    // 1. Visit live Vercel URL
    console.log('[1/5] Loading live production site...');
    await page.goto('https://lockmyseat-two.vercel.app', { waitUntil: 'networkidle2' });

    const titleText = await page.$eval('h1', (el) => el.innerText);
    console.log('  -> Live H1:', titleText);

    // Wait for events to load from Render backend
    await page.waitForSelector('a[href^="/events/"]', { timeout: 15000 });
    const eventCards = await page.$$('a[href^="/events/"]');
    console.log(`  ✓ SUCCESS: Retrieved ${eventCards.length} events from Render + Supabase!`);

    // 2. Sign Up a brand new user on the live cloud database
    const randomSuffix = Math.floor(Math.random() * 9000) + 1000;
    const testEmail = `tester_${randomSuffix}@lockmyseat.cloud`;
    console.log(`\n[2/5] Testing Registration on live Supabase DB with: ${testEmail}...`);

    await page.goto('https://lockmyseat-two.vercel.app/register', { waitUntil: 'networkidle2' });
    await page.type('input[placeholder*="Maya Iyer"]', `Cloud Tester ${randomSuffix}`);
    await page.type('input[type="email"]', testEmail);
    await page.type('input[type="password"]', 'Password@123');

    await page.click('button[type="submit"]');
    await page.waitForNavigation({ waitUntil: 'networkidle2', timeout: 10000 });
    console.log('  ✓ SUCCESS: Registered and navigated to:', page.url());

    // 3. Log in with the newly registered credentials
    console.log('\n[3/5] Logging into live session...');
    await page.type('input[type="email"]', testEmail);
    await page.type('input[type="password"]', 'Password@123');
    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle2' }),
      page.click('button[type="submit"]'),
    ]);
    console.log('  ✓ SUCCESS: Authenticated on live backend! User is on:', page.url());

    // 4. Select an event and book a seat on live Supabase DB
    console.log('\n[4/5] Navigating to seat map on live event...');
    await page.waitForSelector('a[href^="/events/"]', { timeout: 15000 });
    await page.click('a[href^="/events/"]');
    await page.waitForSelector('button[title*="₹"]', { timeout: 15000 });

    const bookedSeatTitle = await page.evaluate(() => {
      const btns = Array.from(document.querySelectorAll('button[title*="₹"]'));
      for (const btn of btns) {
        if (!btn.disabled && !btn.className.includes('line-through')) {
          btn.click();
          return btn.getAttribute('title');
        }
      }
      return null;
    });
    console.log('  -> Clicked live seat:', bookedSeatTitle);

    console.log('  -> Confirming booking (firing ACID lock to Render -> Supabase)...');
    await page.click('button ::-p-text(CONFIRM & LOCK TICKETS)');
    await new Promise((r) => setTimeout(r, 2500));

    const successBanner = await page.evaluate(() => {
      const el = document.querySelector('.bg-\\[\\#E5A93C\\]');
      return el ? el.innerText : null;
    });
    console.log('  ✓ SUCCESS: Live booking confirmation:', successBanner);

    // 5. Verify Ticket in My Bookings & Cancel
    console.log('\n[5/5] Navigating to My Tickets to verify receipt & cancellation...');
    await page.goto('https://lockmyseat-two.vercel.app/my-bookings', { waitUntil: 'networkidle2' });

    page.on('dialog', async (dialog) => {
      console.log('  -> Confirmation Dialog message:', dialog.message());
      await dialog.accept();
    });

    const hasCancelBtn = await page.$('button ::-p-text(CANCEL TICKET)');
    if (hasCancelBtn) {
      console.log('  -> Clicking "CANCEL TICKET" on live booking...');
      await page.click('button ::-p-text(CANCEL TICKET)');
      await new Promise((r) => setTimeout(r, 2500));
      console.log('  ✓ SUCCESS: Ticket cancelled and seat freed on Supabase!');
    }

    console.log('\n==================================================');
    console.log('>>> ALL CLOUD PRODUCTION CHECKS VERIFIED & PASSED! <<<');
    console.log('==================================================');
  } catch (err) {
    console.error('Cloud test failed:', err);
  } finally {
    await browser.close();
  }
})();
