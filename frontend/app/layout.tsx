import type { Metadata } from 'next';
import './globals.css';
import Navbar from '@/components/Navbar';

export const metadata: Metadata = {
  title: 'LockMySeat // Bauhaus & Acid Grunge Minimalist Ticketing',
  description: 'Pure OOP Java 21 Event Ticketing Engine. Zero frameworks. ACID Double-Booking Prevention.',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className="min-h-screen bg-[#F4EFE6] text-[#121212] flex flex-col antialiased selection:bg-[#E5A93C] selection:text-[#121212]">
        <Navbar />
        <main className="flex-1">{children}</main>
        <footer className="border-t-2 border-[#121212] bg-[#EBE4D5] py-8 text-center text-xs font-mono text-[#121212]/80">
          <div className="max-w-6xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-4">
            <div>
              <span className="font-bold">LOCK_MY_SEAT</span> • PROTOTYPE REF #2026.11
            </div>
            <div className="flex items-center space-x-3 text-[11px]">
              <span className="bg-[#121212] text-[#F4EFE6] px-1.5 py-0.5">JAVA 21</span>
              <span className="bg-[#1B449C] text-white px-1.5 py-0.5">POSTGRESQL 18</span>
              <span className="bg-[#D9381E] text-white px-1.5 py-0.5">NO FRAMEWORKS</span>
            </div>
          </div>
        </footer>
      </body>
    </html>
  );
}
