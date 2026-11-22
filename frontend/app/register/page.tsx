'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { apiRequest } from '@/lib/api';

export default function RegisterPage() {
  const router = useRouter();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      await apiRequest('/auth/register', {
        method: 'POST',
        body: JSON.stringify({ name, email, password }),
      });

      setSuccess(true);
      setTimeout(() => {
        router.push('/login');
      }, 1500);
    } catch (err: any) {
      setError(err.message || 'REGISTRATION FAILED.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto my-16 px-4 font-mono">
      <div className="border-3 border-[#121212] bg-[#EBE4D5] p-8 shadow-[6px_6px_0px_#121212]">
        <div className="border-b-2 border-[#121212] pb-4 mb-6">
          <div className="inline-block bg-[#121212] text-white text-[10px] uppercase font-bold px-1.5 py-0.5 mb-2">
            REGISTRATION // NEW_CLIENT
          </div>
          <h1 className="text-2xl font-black text-[#121212] uppercase tracking-tight">CREATE_ACCOUNT</h1>
        </div>

        {error && (
          <div className="mb-6 p-3 border-2 border-[#D9381E] bg-[#D9381E]/10 text-[#D9381E] text-xs font-bold uppercase">
            {error}
          </div>
        )}

        {success && (
          <div className="mb-6 p-3 border-2 border-[#121212] bg-[#E5A93C] text-[#121212] text-xs font-black uppercase">
            ACCOUNT COMMITTED! REDIRECTING TO LOGIN GATE...
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-[10px] font-bold uppercase text-[#121212] mb-1">
              FULL NAME
            </label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Maya Iyer"
              className="w-full bg-[#F4EFE6] border-2 border-[#121212] px-3 py-2 text-xs text-[#121212] placeholder-[#121212]/40 focus:outline-none focus:bg-white font-mono"
            />
          </div>

          <div>
            <label className="block text-[10px] font-bold uppercase text-[#121212] mb-1">
              EMAIL ADDRESS
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
              PASSPHRASE (MIN 6 CHARS)
            </label>
            <input
              type="password"
              required
              minLength={6}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              className="w-full bg-[#F4EFE6] border-2 border-[#121212] px-3 py-2 text-xs text-[#121212] placeholder-[#121212]/40 focus:outline-none focus:bg-white font-mono"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-[#1B449C] hover:bg-[#121212] text-white font-black uppercase py-2.5 border-2 border-[#121212] shadow-[3px_3px_0px_#121212] text-xs transition disabled:opacity-40 mt-2"
          >
            {loading ? 'REGISTERING...' : 'REGISTER IDENTITY →'}
          </button>
        </form>

        <p className="text-center text-xs text-[#121212]/70 mt-6 font-mono">
          ALREADY REGISTERED?{' '}
          <Link href="/login" className="font-bold underline text-[#D9381E] hover:text-[#121212]">
            LOG IN HERE
          </Link>
        </p>
      </div>
    </div>
  );
}
