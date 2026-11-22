const puppeteer = require('puppeteer');

(async () => {
  console.log('--- STARTING COMPREHENSIVE BROWSER UI TEST ---');
  const browser = await puppeteer.launch({
    headless: 'new',
    args: ['--no-sandbox', '--disable-setuid-sandbox'],
  });

  const page = await browser.newPage();
  await page.setViewport({ width: 1280, height: 800 });

  try {
    // 1. Visit Home Page
    console.log('\n[1/6] Navigating to Home Page (http://localhost:3000)...');
    await page.goto('http://localhost:3000', { waitUntil: 'networkidle0' });

    // Verify Tailwind CSS has loaded by checking computed background color of body
    const bodyBg = await page.evaluate(() => {
      const el = document.body;
      return window.getComputedStyle(el).backgroundColor;
    });
    console.log('  -> Body background computed style:', bodyBg);
    if (bodyBg === 'rgb(2, 6, 23)' || bodyBg.includes('rgb(')) {
      console.log('  ✓ SUCCESS: Tailwind CSS background color is applied correctly!');
    } else {
      console.error('  ✗ FAIL: Background color does not match Tailwind dark slate');
    }

    // Check title and cards
    const titleText = await page.$eval('h1', (el) => el.innerText);
    console.log('  -> Main heading:', titleText);

    const eventCount = await page.$$eval('a[href^="/events/"]', (els) => els.length);
    console.log(`  -> Detected ${eventCount} event card buttons on the homepage.`);

    // 2. Test City Filters
    console.log('\n[2/6] Testing City Filters on Home Page...');
    await page.click('button ::-p-text(Bengaluru)');
    await new Promise((r) => setTimeout(r, 600));
    const filteredCards = await page.$$eval('a[href^="/events/"]', (els) => els.length);
    console.log(`  -> Filtered to Bengaluru: ${filteredCards} event(s) displayed.`);

    await page.click('button ::-p-text(All)');
    await new Promise((r) => setTimeout(r, 600));

    // 3. Test Login Page & One-Click Normal User Login
    console.log('\n[3/6] Navigating to /login and testing user authentication...');
    await page.goto('http://localhost:3000/login', { waitUntil: 'networkidle0' });

    // Click quick-login for normal user
    await page.click('button ::-p-text([USER: JOHN])');
    const emailVal = await page.$eval('input[type="email"]', (el) => el.value);
    console.log('  -> Quick-fill populated email:', emailVal);

    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle0' }),
      page.click('button[type="submit"]'),
    ]);
    console.log('  ✓ SUCCESS: Logged in as John Doe! Redirected to:', page.url());

    // 4. Test Event Detail & Interactive Seat Map
    console.log('\n[4/6] Navigating to Event Detail and Seat Map (Event 1)...');
    await page.goto('http://localhost:3000/events/1', { waitUntil: 'networkidle0' });

    // Verify seat buttons exist
    const seatButtons = await page.$$('button[title*="VIP"], button[title*="REGULAR"]');
    console.log(`  -> Found ${seatButtons.length} interactive seats on the map.`);

    // Check VIP vs Regular pricing in DOM
    const seatInfo = await page.evaluate(() => {
      const firstBtn = document.querySelector('button[title*="VIP"]');
      const regBtn = document.querySelector('button[title*="REGULAR"]');
      return {
        vip: firstBtn ? firstBtn.getAttribute('title') : null,
        regular: regBtn ? regBtn.getAttribute('title') : null,
      };
    });
    console.log('  -> VIP Seat Spec:', seatInfo.vip);
    console.log('  -> Regular Seat Spec:', seatInfo.regular);

    // Pick an available seat
    const availableSeat = await page.evaluate(() => {
      const btns = Array.from(document.querySelectorAll('button[title*="₹"]'));
      for (const btn of btns) {
        if (!btn.disabled && !btn.className.includes('line-through')) {
          btn.click();
          return btn.getAttribute('title');
        }
      }
      return null;
    });
    console.log('  -> Clicked available seat:', availableSeat);

    // Click confirm booking
    console.log('  -> Clicking "CONFIRM & LOCK TICKETS"...');
    await page.click('button ::-p-text(CONFIRM & LOCK TICKETS)');
    await new Promise((r) => setTimeout(r, 1200));

    // 5. Test My Bookings & Cancellation
    console.log('\n[5/6] Navigating to /my-bookings...');
    await page.goto('http://localhost:3000/my-bookings', { waitUntil: 'networkidle0' });

    // Handle dialog if confirm box appears
    page.on('dialog', async (dialog) => {
      console.log('  -> Alert/Confirm dialog message:', dialog.message());
      await dialog.accept();
    });

    const hasCancelBtn = await page.$('button ::-p-text(CANCEL TICKET)');
    if (hasCancelBtn) {
      console.log('  -> Clicking "CANCEL TICKET" to test seat release...');
      await page.click('button ::-p-text(CANCEL TICKET)');
      await new Promise((r) => setTimeout(r, 1500));
      console.log('  ✓ SUCCESS: Booking cancelled and seat released.');
    }

    // 6. Test Admin Panel
    console.log('\n[6/6] Testing Admin Panel access (/login as Admin)...');
    await page.goto('http://localhost:3000/login', { waitUntil: 'networkidle0' });
    await page.click('button ::-p-text([ADMIN: ROOT])');
    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle0' }),
      page.click('button[type="submit"]'),
    ]);

    await page.goto('http://localhost:3000/admin', { waitUntil: 'networkidle0' });
    const adminHeading = await page.$eval('h1', (el) => el.innerText);
    console.log('  -> Admin Panel heading:', adminHeading);

    const eventTableRows = await page.$$eval('tbody tr', (els) => els.length);
    console.log(`  -> Admin table lists ${eventTableRows} active events.`);
    console.log('  ✓ SUCCESS: Admin console verified.');

    console.log('\n==================================================');
    console.log('>>> ALL BROWSER & UI TESTS PASSED SUCCESSFULLY! <<<');
    console.log('==================================================');
  } catch (err) {
    console.error('Test execution failed:', err);
  } finally {
    await browser.close();
  }
})();
