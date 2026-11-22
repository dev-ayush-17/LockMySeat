'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { User, getStoredUser, clearSession, apiRequest } from '@/lib/api';

export default function Navbar() {
  const router = useRouter();
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    setUser(getStoredUser());

    const handleAuthChange = () => {
      setUser(getStoredUser());
    };
    window.addEventListener('storage', handleAuthChange);
    window.addEventListener('lms_auth_changed', handleAuthChange);
    return () => {
      window.removeEventListener('storage', handleAuthChange);
      window.removeEventListener('lms_auth_changed', handleAuthChange);
    };
  }, []);

  const handleLogout = async () => {
    try {
      await apiRequest('/auth/logout', { method: 'POST' });
    } catch {
      // ignore
    } finally {
      clearSession();
      setUser(null);
      window.dispatchEvent(new Event('lms_auth_changed'));
      router.push('/');
    }
  };

  return (
    <header className="border-b-2 border-[#121212] bg-[#F4EFE6] sticky top-0 z-50">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between">
        {/* Brand Bauhaus Mark */}
        <Link href="/" className="flex items-center space-x-3 group">
          <div className="w-8 h-8 bg-[#D9381E] border-2 border-[#121212] flex items-center justify-center font-black text-white text-sm shadow-[2px_2px_0px_#121212] group-hover:translate-x-0.5 group-hover:translate-y-0.5 transition-transform">
            ■
          </div>
          <div className="flex flex-col">
            <span className="font-mono font-black text-lg tracking-tighter uppercase text-[#121212]">
              LOCK_MY_SEAT
            </span>
            <span className="text-[9px] uppercase tracking-widest text-[#121212]/60 font-mono -mt-1">
              OOP_LAB // VER.21
            </span>
          </div>
        </Link>

        {/* Navigation */}
        <nav className="flex items-center space-x-3 sm:space-x-5 text-xs font-mono font-bold tracking-tight">
          <Link
            href="/"
            className="text-[#121212] hover:text-[#D9381E] uppercase px-2 py-1 transition"
          >
            [Events]
          </Link>

          {user ? (
            <>
              <Link
                href="/my-bookings"
                className="text-[#121212] hover:text-[#D9381E] uppercase px-2 py-1 transition"
              >
                [My_Tickets]
              </Link>

              {user.role === 'ADMIN' && (
                <Link
                  href="/admin"
                  className="bg-[#E5A93C] text-[#121212] px-2.5 py-1 border-2 border-[#121212] shadow-[2px_2px_0px_#121212] uppercase font-black hover:translate-x-0.5 hover:translate-y-0.5 transition"
                >
                  ⚡ Admin
                </Link>
              )}

              <div className="flex items-center pl-3 border-l-2 border-[#121212] space-x-3">
                <span className="text-[11px] text-[#121212]">
                  USER: <span className="underline decoration-[#D9381E] font-bold">{user.name.split(' ')[0]}</span>
                </span>
                <button
                  onClick={handleLogout}
                  className="bg-[#121212] text-[#F4EFE6] px-2 py-1 text-[10px] uppercase font-mono tracking-wider hover:bg-[#D9381E] transition"
                >
                  EXIT
                </button>
              </div>
            </>
          ) : (
            <div className="flex items-center space-x-2">
              <Link
                href="/login"
                className="text-[#121212] hover:text-[#D9381E] uppercase px-2 py-1 transition"
              >
                LOGIN
              </Link>
              <Link
                href="/register"
                className="bg-[#D9381E] text-white px-3 py-1.5 border-2 border-[#121212] shadow-[2px_2px_0px_#121212] uppercase font-bold hover:translate-x-0.5 hover:translate-y-0.5 transition"
              >
                SIGN_UP →
              </Link>
            </div>
          )}
        </nav>
      </div>
    </header>
  );
}
