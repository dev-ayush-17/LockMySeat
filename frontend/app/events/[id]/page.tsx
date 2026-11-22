'use client';

import { use, useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Event, Seat, apiRequest, getStoredToken } from '@/lib/api';

export default function EventDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const resolvedParams = use(params);
  const eventId = resolvedParams.id;
  const router = useRouter();

  const [event, setEvent] = useState<Event | null>(null);
  const [seats, setSeats] = useState<Seat[]>([]);
  const [selectedSeatIds, setSelectedSeatIds] = useState<number[]>([]);
  const [loading, setLoading] = useState(true);
  const [bookingLoading, setBookingLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  const loadData = async () => {
    try {
      const [evtData, seatsData] = await Promise.all([
        apiRequest<Event>(`/events/${eventId}`),
        apiRequest<Seat[]>(`/events/${eventId}/seats`),
      ]);
      setEvent(evtData);
      setSeats(seatsData);
    } catch (err: any) {
      setError(err.message || 'Failed to load event details.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [eventId]);

  const toggleSeat = (seat: Seat) => {
    if (seat.status === 'BOOKED') return;

    if (selectedSeatIds.includes(seat.id)) {
      setSelectedSeatIds(selectedSeatIds.filter((id) => id !== seat.id));
    } else {
      if (selectedSeatIds.length >= 4) {
        setError('RULE LIMIT: MAXIMUM 4 SEATS PER TRANSACTION.');
        return;
      }
      setError(null);
      setSelectedSeatIds([...selectedSeatIds, seat.id]);
    }
  };

  const calculateTotal = () => {
    return selectedSeatIds.reduce((sum, id) => {
      const s = seats.find((st) => st.id === id);
      return sum + (s ? s.price : 0);
    }, 0);
  };

  const handleBooking = async () => {
    const token = getStoredToken();
    if (!token) {
      router.push('/login');
      return;
    }

    if (selectedSeatIds.length === 0) {
      setError('SELECTION REQUIRED: CLICK AT LEAST ONE SEAT.');
      return;
    }

    setBookingLoading(true);
    setError(null);
    setSuccessMsg(null);

    try {
      const booking = await apiRequest<any>('/bookings', {
        method: 'POST',
        body: JSON.stringify({
          eventId: Number(eventId),
          seatIds: selectedSeatIds,
        }),
      });

      setSuccessMsg(`TICKETS LOCKED: ORDER #${booking.id} COMMITTED FOR ₹${booking.totalAmount}`);
      setSelectedSeatIds([]);
      await loadData();
    } catch (err: any) {
      setError(err.message || 'TRANSACTION REJECTED: CONFLICT DETECTED.');
      await loadData();
    } finally {
      setBookingLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-20 text-center font-mono">
        <div className="inline-block p-3 border-2 border-[#121212] bg-[#EBE4D5] shadow-[4px_4px_0px_#121212]">
          READING_SEAT_MAP_DATA...
        </div>
      </div>
    );
  }

  if (!event) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-20 text-center font-mono">
        <p className="text-xs uppercase font-bold text-[#D9381E]">EVENT RECORD NOT LOCATED.</p>
        <Link href="/" className="mt-4 inline-block text-xs uppercase underline text-[#121212]">
          ← RETURN TO EVENTS
        </Link>
      </div>
    );
  }

  const rows = ['A', 'B', 'C', 'D', 'E'];

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 py-10 font-mono">
      {/* Event Header Banner */}
      <div className="border-3 border-[#121212] bg-[#EBE4D5] p-6 mb-8 shadow-[6px_6px_0px_#121212]">
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 border-b-2 border-[#121212] pb-4 mb-4">
          <div>
            <div className="flex items-center gap-2 mb-1">
              <span className="text-[10px] font-black uppercase bg-[#121212] text-[#F4EFE6] px-2 py-0.5">
                CITY: {event.city}
              </span>
              <span className="text-xs font-bold text-[#121212]/80">VENUE: {event.venue}</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-black text-[#121212] uppercase tracking-tight">
              {event.title}
            </h1>
          </div>

          <div className="text-left sm:text-right border-2 border-[#121212] bg-[#F4EFE6] p-2.5 shadow-[2px_2px_0px_#121212]">
            <div className="text-[9px] uppercase text-[#121212]/60">DATE OF EVENT</div>
            <div className="text-xs font-black text-[#121212]">
              {new Date(event.eventDate).toLocaleDateString('en-IN', {
                weekday: 'short',
                day: '2-digit',
                month: 'short',
                year: 'numeric',
              })}
            </div>
          </div>
        </div>

        <p className="text-xs text-[#121212]/70 font-sans">{event.description}</p>
      </div>

      {/* Status Banners */}
      {error && (
        <div className="mb-6 p-3 border-2 border-[#D9381E] bg-[#D9381E]/10 text-[#D9381E] text-xs font-bold uppercase shadow-[3px_3px_0px_#D9381E]">
          {error}
        </div>
      )}

      {successMsg && (
        <div className="mb-6 p-3 border-2 border-[#121212] bg-[#E5A93C] text-[#121212] text-xs font-black uppercase shadow-[3px_3px_0px_#121212] flex items-center justify-between">
          <span>{successMsg}</span>
          <Link href="/my-bookings" className="underline ml-4 text-[11px]">
            VIEW RECEIPT →
          </Link>
        </div>
      )}

      {/* Main Grid: Bauhaus Seat Matrix + Order Ticket */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left: Industrial Seat Matrix */}
        <div className="lg:col-span-2 border-2 border-[#121212] bg-[#EBE4D5] p-6 shadow-[5px_5px_0px_#121212] flex flex-col items-center">
          {/* Bauhaus Stage Screen Line */}
          <div className="w-full max-w-md mx-auto mb-10 text-center">
            <div className="h-3 bg-[#121212] border-b-2 border-white mb-1.5" />
            <div className="text-[10px] tracking-widest uppercase font-black text-[#121212]/70">
              ▼ [PROJECTION_SCREEN / STAGE_APEX] ▼
            </div>
          </div>

          {/* Seat Grid */}
          <div className="space-y-2.5 w-full max-w-md">
            {rows.map((rowLabel) => {
              const rowSeats = seats.filter((s) => s.rowLabel === rowLabel);
              const isVipRow = rowLabel === 'A' || rowLabel === 'B';

              return (
                <div key={rowLabel} className="flex items-center justify-between gap-2">
                  <span className="w-5 text-xs font-black text-[#121212] text-center">
                    {rowLabel}
                  </span>

                  <div className="flex-1 flex justify-center gap-2">
                    {rowSeats.map((seat) => {
                      const isSelected = selectedSeatIds.includes(seat.id);
                      const isBooked = seat.status === 'BOOKED';

                      let btnStyle = 'bg-[#F4EFE6] text-[#121212] border-2 border-[#121212] hover:bg-white shadow-[2px_2px_0px_#121212]';

                      if (isBooked) {
                        btnStyle = 'bg-[#121212]/20 text-[#121212]/30 border-2 border-dashed border-[#121212]/30 cursor-not-allowed line-through';
                      } else if (isSelected) {
                        btnStyle = 'bg-[#D9381E] text-white border-2 border-[#121212] font-black shadow-[3px_3px_0px_#121212] -translate-x-0.5 -translate-y-0.5';
                      } else if (isVipRow) {
                        btnStyle = 'bg-[#1B449C] text-white border-2 border-[#121212] shadow-[2px_2px_0px_#121212] hover:bg-[#1B449C]/90';
                      }

                      return (
                        <button
                          key={seat.id}
                          disabled={isBooked}
                          onClick={() => toggleSeat(seat)}
                          title={`${seat.seatNumber} (${seat.seatType}) - ₹${seat.price}`}
                          className={`w-9 h-9 text-xs font-mono font-bold transition-all flex items-center justify-center ${btnStyle}`}
                        >
                          {seat.seatNumber}
                        </button>
                      );
                    })}
                  </div>

                  <span className="w-12 text-[10px] font-bold text-[#121212]/60 text-right">
                    {isVipRow ? '₹500' : '₹250'}
                  </span>
                </div>
              );
            })}
          </div>

          {/* Seat Legend */}
          <div className="mt-10 pt-4 border-t-2 border-[#121212] w-full flex flex-wrap items-center justify-center gap-4 text-[11px] font-bold">
            <div className="flex items-center gap-1.5">
              <span className="w-4 h-4 bg-[#1B449C] border border-[#121212]"></span>
              <span>VIP (₹500)</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="w-4 h-4 bg-[#F4EFE6] border border-[#121212]"></span>
              <span>REGULAR (₹250)</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="w-4 h-4 bg-[#D9381E] border border-[#121212]"></span>
              <span>SELECTED</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="w-4 h-4 bg-[#121212]/20 border border-dashed border-[#121212]/40 line-through text-[9px] flex items-center justify-center">
                ✕
              </span>
              <span>TAKEN</span>
            </div>
          </div>
        </div>

        {/* Right: Brutalist Order Ticket Voucher */}
        <div className="border-2 border-[#121212] bg-[#F4EFE6] p-6 shadow-[5px_5px_0px_#121212] flex flex-col justify-between h-fit relative">
          <div className="border-b-2 border-dashed border-[#121212] pb-4 mb-4">
            <div className="text-[10px] uppercase font-bold text-[#121212]/60">VOUCHER SUMMARY</div>
            <h2 className="text-lg font-black uppercase text-[#121212]">SELECTION_MANIFEST</h2>
          </div>

          <div className="space-y-3">
            <div className="flex justify-between text-xs">
              <span className="text-[#121212]/70">SEATS CHOSEN:</span>
              <span className="font-bold text-[#121212]">{selectedSeatIds.length} / 4 MAX</span>
            </div>

            {selectedSeatIds.length === 0 ? (
              <div className="py-8 text-center text-xs text-[#121212]/50 border-2 border-dashed border-[#121212]/30">
                [CLICK SEATS ON GRID TO ADD]
              </div>
            ) : (
              <div className="space-y-1.5 py-1">
                {selectedSeatIds.map((id) => {
                  const s = seats.find((st) => st.id === id);
                  if (!s) return null;
                  return (
                    <div
                      key={id}
                      className="flex items-center justify-between border border-[#121212] bg-[#EBE4D5] px-2.5 py-1 text-xs"
                    >
                      <span className="font-black text-[#D9381E]">{s.seatNumber} ({s.seatType})</span>
                      <span className="font-bold text-[#121212]">₹{s.price}</span>
                    </div>
                  );
                })}
              </div>
            )}

            <div className="pt-4 border-t-2 border-[#121212] flex justify-between items-baseline">
              <span className="text-xs uppercase font-bold text-[#121212]">TOTAL FEE:</span>
              <span className="text-2xl font-black text-[#121212]">₹{calculateTotal()}</span>
            </div>
          </div>

          <div className="mt-8 pt-4 border-t-2 border-dashed border-[#121212]">
            <button
              onClick={handleBooking}
              disabled={selectedSeatIds.length === 0 || bookingLoading}
              className="w-full bg-[#121212] hover:bg-[#D9381E] disabled:opacity-30 text-[#F4EFE6] font-black uppercase py-3 border-2 border-[#121212] shadow-[3px_3px_0px_#121212] transition"
            >
              {bookingLoading ? 'COMMIT TRANSACTION...' : 'CONFIRM & LOCK TICKETS'}
            </button>
            <p className="text-[9px] uppercase text-center text-[#121212]/60 mt-2 font-mono">
              // PREVENTS DOUBLE-BOOKING VIA JAVA TRANSACTION GUARD
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
