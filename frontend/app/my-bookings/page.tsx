'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Booking, apiRequest, getStoredToken } from '@/lib/api';

export default function MyBookingsPage() {
  const router = useRouter();
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoadingId, setActionLoadingId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  const fetchBookings = async () => {
    const token = getStoredToken();
    if (!token) {
      router.push('/login');
      return;
    }

    try {
      const data = await apiRequest<Booking[]>('/bookings/my');
      setBookings(data);
    } catch (err: any) {
      setError(err.message || 'COULD NOT RETRIEVE TICKETS.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBookings();
  }, []);

  const handleCancel = async (bookingId: number) => {
    if (!confirm('CONFIRM CANCELLATION: Seats will be released back to the global pool immediately.')) {
      return;
    }

    setActionLoadingId(bookingId);
    setError(null);
    setNotice(null);

    try {
      await apiRequest<{ message: string }>(`/bookings/${bookingId}/cancel`, {
        method: 'POST',
      });
      setNotice('SEATS RELEASED: Your booking has been cancelled and seats returned to inventory.');
      await fetchBookings();
    } catch (err: any) {
      setError(err.message || 'CANCELLATION FAILED.');
    } finally {
      setActionLoadingId(null);
    }
  };

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-20 text-center font-mono">
        <div className="inline-block p-3 border-2 border-[#121212] bg-[#EBE4D5] shadow-[4px_4px_0px_#121212]">
          LOADING_TICKETS_MANIFEST...
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 py-10 font-mono">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-8 border-b-2 border-[#121212] pb-4">
        <div>
          <div className="inline-block bg-[#121212] text-white text-[10px] uppercase font-bold px-1.5 py-0.5 mb-1">
            CLIENT_LEDGER // ARCHIVE
          </div>
          <h1 className="text-2xl sm:text-3xl font-black text-[#121212] uppercase tracking-tight">MY_RESERVATIONS</h1>
        </div>
        <Link
          href="/"
          className="text-xs bg-[#EBE4D5] border-2 border-[#121212] shadow-[2px_2px_0px_#121212] hover:bg-white text-[#121212] font-black uppercase px-3 py-1.5 transition"
        >
          + BROWSE MORE EVENTS
        </Link>
      </div>

      {error && (
        <div className="mb-6 p-3 border-2 border-[#D9381E] bg-[#D9381E]/10 text-[#D9381E] text-xs font-bold uppercase">
          {error}
        </div>
      )}

      {notice && (
        <div className="mb-6 p-3 border-2 border-[#121212] bg-[#E5A93C] text-[#121212] text-xs font-black uppercase shadow-[3px_3px_0px_#121212]">
          {notice}
        </div>
      )}

      {bookings.length === 0 ? (
        <div className="text-center py-20 border-2 border-dashed border-[#121212]/40 bg-[#EBE4D5]">
          <p className="text-xs font-bold uppercase text-[#121212]/60">NO ACTIVE TICKETS FOUND IN REPOSITORY.</p>
          <Link
            href="/"
            className="mt-4 inline-block bg-[#D9381E] text-white font-black text-xs uppercase px-4 py-2 border-2 border-[#121212] shadow-[3px_3px_0px_#121212] hover:translate-x-0.5 hover:translate-y-0.5 transition"
          >
            DISCOVER UPCOMING EVENTS →
          </Link>
        </div>
      ) : (
        <div className="space-y-4">
          {bookings.map((b) => {
            const isConfirmed = b.status === 'CONFIRMED';
            const bookingDate = new Date(b.bookingDate).toLocaleDateString('en-IN', {
              day: '2-digit',
              month: 'short',
              year: 'numeric',
              hour: '2-digit',
              minute: '2-digit',
            });

            return (
              <div
                key={b.id}
                className="border-2 border-[#121212] bg-[#EBE4D5] p-5 flex flex-col md:flex-row justify-between items-start md:items-center gap-4 shadow-[4px_4px_0px_#121212]"
              >
                <div className="space-y-1.5 flex-1">
                  <div className="flex items-center gap-2">
                    <span className="text-[10px] font-black uppercase text-[#121212]">REC #{b.id}</span>
                    <span
                      className={`text-[9px] font-black px-1.5 py-0.5 uppercase tracking-wider border border-[#121212] ${
                        isConfirmed
                          ? 'bg-[#121212] text-white'
                          : 'bg-[#D9381E] text-white line-through'
                      }`}
                    >
                      {b.status}
                    </span>
                    <span className="text-[10px] text-[#121212]/60">• {bookingDate}</span>
                  </div>

                  <h3 className="text-base font-black text-[#121212] uppercase">
                    {b.eventTitle || `EVENT REF #${b.eventId}`}
                  </h3>
                  <p className="text-[11px] text-[#121212]/70">LOCATION: {b.eventCity || 'INDIAN VENUE'}</p>

                  <div className="flex flex-wrap items-center gap-1.5 pt-1">
                    <span className="text-[10px] font-bold text-[#121212]/60">SEATS_LOCKED:</span>
                    {b.items && b.items.length > 0 ? (
                      b.items.map((item) => (
                        <span
                          key={item.id}
                          className="px-1.5 py-0.5 bg-[#F4EFE6] border border-[#121212] text-[11px] font-mono font-black text-[#D9381E]"
                        >
                          {item.seatNumber}
                        </span>
                      ))
                    ) : (
                      <span className="text-[10px] text-[#121212]/40 italic">RELEASED</span>
                    )}
                  </div>
                </div>

                <div className="flex md:flex-col items-center md:items-end justify-between w-full md:w-auto gap-3 border-t md:border-t-0 pt-3 md:pt-0 border-[#121212]/30">
                  <div className="text-left md:text-right">
                    <div className="text-[9px] uppercase tracking-wider text-[#121212]/60">
                      TRANSACTION SUM
                    </div>
                    <div className="text-xl font-black text-[#121212]">
                      ₹{b.totalAmount}
                    </div>
                  </div>

                  {isConfirmed && (
                    <button
                      onClick={() => handleCancel(b.id)}
                      disabled={actionLoadingId === b.id}
                      className="bg-[#D9381E] hover:bg-[#121212] text-white text-[11px] font-black uppercase px-3 py-1.5 border-2 border-[#121212] shadow-[2px_2px_0px_#121212] transition disabled:opacity-40"
                    >
                      {actionLoadingId === b.id ? 'RELEASING...' : 'CANCEL TICKET'}
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
