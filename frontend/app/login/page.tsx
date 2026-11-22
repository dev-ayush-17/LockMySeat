'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { apiRequest, setSession } from '@/lib/api';

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const data = await apiRequest<{
        token: string;
        userId: number;
        name: string;
        email: string;
        role: 'USER' | 'ADMIN';
      }>('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password }),
      });

      setSession(data.token, {
        id: data.userId,
        name: data.name,
        email: data.email,
        role: data.role,
      });

      window.dispatchEvent(new Event('lms_auth_changed'));
      router.push('/');
    } catch (err: any) {
      setError(err.message || 'AUTHENTICATION REJECTED. CHECK CREDENTIALS.');
    } finally {
      setLoading(false);
    }
  };

  const fillQuickLogin = (role: 'admin' | 'user') => {
    if (role === 'admin') {
      setEmail('admin@lockmyseat.com');
      setPassword('Admin@123');
    } else {
      setEmail('john@example.com');
      setPassword('User@123');
    }
  };

  return (
    <div className="max-w-md mx-auto my-16 px-4 font-mono">
      <div className="border-3 border-[#121212] bg-[#EBE4D5] p-8 shadow-[6px_6px_0px_#121212]">
        <div className="border-b-2 border-[#121212] pb-4 mb-6">
          <div className="inline-block bg-[#121212] text-white text-[10px] uppercase font-bold px-1.5 py-0.5 mb-2">
            GATE_ACCESS // AUTH
          </div>
          <h1 className="text-2xl font-black text-[#121212] uppercase tracking-tight">IDENTITY_VERIFICATION</h1>
        </div>

        {error && (
          <div className="mb-6 p-3 border-2 border-[#D9381E] bg-[#D9381E]/10 text-[#D9381E] text-xs font-bold uppercase">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-[10px] font-bold uppercase text-[#121212] mb-1">
              EMAIL IDENTIFIER
            </label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="user@example.com"
              className="w-full bg-[#F4EFE6] border-2 border-[#121212] px-3 py-2 text-xs text-[#121212] placeholder-[#121212]/40 focus:outline-none focus:bg-white font-mono"
            />
          </div>

          <div>
            <label className="block text-[10px] font-bold uppercase text-[#121212] mb-1">
              PASSPHRASE
            </label>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              className="w-full bg-[#F4EFE6] border-2 border-[#121212] px-3 py-2 text-xs text-[#121212] placeholder-[#121212]/40 focus:outline-none focus:bg-white font-mono"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-[#D9381E] hover:bg-[#121212] text-white font-black uppercase py-2.5 border-2 border-[#121212] shadow-[3px_3px_0px_#121212] text-xs transition disabled:opacity-40"
          >
            {loading ? 'AUTHENTICATING...' : 'ENTER →'}
          </button>
        </form>

        <div className="mt-8 pt-6 border-t-2 border-dashed border-[#121212] space-y-2">
          <div className="text-[10px] font-bold uppercase text-[#121212]/60">PRE-SEEDED IDENTITIES:</div>
          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => fillQuickLogin('user')}
              className="flex-1 py-1.5 px-2 bg-[#F4EFE6] hover:bg-white border-2 border-[#121212] text-[10px] font-black uppercase shadow-[2px_2px_0px_#121212] transition"
            >
              [USER: JOHN]
            </button>
            <button
              type="button"
              onClick={() => fillQuickLogin('admin')}
              className="flex-1 py-1.5 px-2 bg-[#E5A93C] hover:bg-[#E5A93C]/80 border-2 border-[#121212] text-[10px] font-black uppercase shadow-[2px_2px_0px_#121212] transition"
            >
              [ADMIN: ROOT]
            </button>
          </div>
        </div>

        <p className="text-center text-xs text-[#121212]/70 mt-6 font-mono">
          UNREGISTERED?{' '}
          <Link href="/register" className="font-bold underline text-[#D9381E] hover:text-[#121212]">
            REGISTER HERE
          </Link>
        </p>
      </div>
    </div>
  );
}
