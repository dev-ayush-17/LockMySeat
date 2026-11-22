'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Event, apiRequest } from '@/lib/api';

export default function HomePage() {
  const [events, setEvents] = useState<Event[]>([]);
  const [search, setSearch] = useState('');
  const [selectedCity, setSelectedCity] = useState('All');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const cities = ['All', 'Mumbai', 'Bengaluru', 'Delhi', 'Hyderabad', 'Chennai', 'Kolkata'];

  const fetchEvents = async () => {
    setLoading(true);
    setError(null);
    try {
      const queryParams = [];
      if (search.trim()) queryParams.push(`search=${encodeURIComponent(search.trim())}`);
      if (selectedCity !== 'All') queryParams.push(`city=${encodeURIComponent(selectedCity)}`);
      
      const queryStr = queryParams.length > 0 ? `?${queryParams.join('&')}` : '';
      const data = await apiRequest<Event[]>(`/events${queryStr}`);
      setEvents(data);
    } catch (err: any) {
      setError(err.message || 'Failed to fetch events.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEvents();
  }, [selectedCity]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchEvents();
  };

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 py-10 font-mono">
      {/* Bauhaus Poster Hero Section */}
      <div className="border-3 border-[#121212] bg-[#EBE4D5] p-6 sm:p-10 mb-10 shadow-[6px_6px_0px_#121212] relative overflow-hidden">
        <div className="absolute top-0 right-0 w-32 h-32 bg-[#E5A93C] rounded-bl-full border-b-2 border-l-2 border-[#121212] -z-0 opacity-80" />
        <div className="absolute bottom-0 right-16 w-16 h-16 bg-[#1B449C] border-t-2 border-l-2 border-r-2 border-[#121212] -z-0 opacity-40" />

        <div className="relative z-10 max-w-3xl">
          <div className="inline-block bg-[#121212] text-[#F4EFE6] text-xs font-bold uppercase px-2 py-0.5 mb-3 tracking-widest">
            TICKETING ARCHITECTURE // ACID DUAL-LOCK
          </div>
          <h1 className="text-3xl sm:text-5xl font-black text-[#121212] tracking-tight uppercase leading-none">
            RESERVE WITHOUT <span className="bg-[#D9381E] text-white px-2 inline-block">COLLISIONS</span>
          </h1>
          <p className="mt-4 text-xs sm:text-sm text-[#121212]/80 leading-relaxed font-sans max-w-2xl">
            A hand-crafted event ticketing prototype constructed entirely in plain Java 21 and PostgreSQL.
            Synchronized thread guards and atomic conditional updates eliminate double-bookings.
          </p>
        </div>
      </div>

      {/* Grunge Search & Filter Toolbar */}
      <div className="border-2 border-[#121212] bg-[#F4EFE6] p-4 mb-8 shadow-[4px_4px_0px_#121212]">
        <form onSubmit={handleSearchSubmit} className="flex flex-col sm:flex-row gap-3">
          <div className="flex-1 relative">
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="SEARCH_EVENTS [TITLE, ARTIST, VENUE]..."
              className="w-full bg-[#EBE4D5] border-2 border-[#121212] px-3 py-2 text-xs text-[#121212] placeholder-[#121212]/40 focus:outline-none focus:bg-white font-mono uppercase"
            />
          </div>

          <div className="flex items-center gap-1.5 overflow-x-auto pb-1 sm:pb-0">
            {cities.map((city) => (
              <button
                key={city}
                type="button"
                onClick={() => setSelectedCity(city)}
                className={`px-2.5 py-1.5 text-[11px] font-bold uppercase border-2 border-[#121212] whitespace-nowrap transition ${
                  selectedCity === city
                    ? 'bg-[#121212] text-white shadow-[2px_2px_0px_#D9381E]'
                    : 'bg-[#EBE4D5] text-[#121212] hover:bg-white'
                }`}
              >
                {city}
              </button>
            ))}
          </div>

          <button
            type="submit"
            className="bg-[#D9381E] text-white font-black text-xs uppercase px-4 py-2 border-2 border-[#121212] shadow-[2px_2px_0px_#121212] hover:translate-x-0.5 hover:translate-y-0.5 transition"
          >
            FILTER
          </button>
        </form>
      </div>

      {/* Error state */}
      {error && (
        <div className="mb-8 p-3 border-2 border-[#D9381E] bg-[#D9381E]/10 text-[#D9381E] text-xs font-bold uppercase">
          ERROR: {error}
        </div>
      )}

      {/* Loading state */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <div key={i} className="h-64 border-2 border-[#121212] bg-[#EBE4D5] animate-pulse shadow-[4px_4px_0px_#121212]" />
          ))}
        </div>
      ) : events.length === 0 ? (
        <div className="text-center py-16 border-2 border-[#121212] bg-[#EBE4D5] shadow-[4px_4px_0px_#121212]">
          <p className="text-xs uppercase font-bold text-[#121212]/60">NO MATCHING EVENTS RECORDED IN SYSTEM.</p>
          <button
            onClick={() => {
              setSearch('');
              setSelectedCity('All');
            }}
            className="mt-3 text-xs uppercase font-bold text-[#D9381E] underline hover:text-[#121212]"
          >
            RESET ALL FILTERS
          </button>
        </div>
      ) : (
        /* Event Cards Grid */
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {events.map((evt, idx) => {
            const dateObj = new Date(evt.eventDate);
            const dateFormatted = dateObj.toLocaleDateString('en-IN', {
              day: '2-digit',
              month: 'short',
              year: 'numeric',
            });
            const timeFormatted = dateObj.toLocaleTimeString('en-IN', {
              hour: '2-digit',
              minute: '2-digit',
            });

            // Accent color alternation for vintage ticket feel
            const accentBg = idx % 3 === 0 ? 'bg-[#D9381E]' : idx % 3 === 1 ? 'bg-[#1B449C]' : 'bg-[#E5A93C]';

            return (
              <div
                key={evt.id}
                className="border-2 border-[#121212] bg-[#EBE4D5] p-5 flex flex-col justify-between shadow-[4px_4px_0px_#121212] hover:shadow-[6px_6px_0px_#121212] hover:-translate-x-0.5 hover:-translate-y-0.5 transition"
              >
                <div>
                  <div className="flex items-center justify-between mb-3 border-b-2 border-[#121212] pb-2">
                    <span className="text-[10px] font-black uppercase tracking-wider text-[#121212]">
                      LOC: {evt.city}
                    </span>
                    <span className="text-[10px] font-bold bg-[#121212] text-[#F4EFE6] px-1.5 py-0.5">
                      {evt.availableSeats}/{evt.totalSeats} SEATS
                    </span>
                  </div>

                  <h2 className="text-base font-black text-[#121212] uppercase tracking-tight leading-tight mb-2">
                    {evt.title}
                  </h2>

                  <p className="text-[#121212]/70 text-xs font-sans mb-4 line-clamp-2">
                    {evt.description || 'Live stage performance.'}
                  </p>

                  <div className="space-y-1 text-[11px] text-[#121212]/90 border-t border-[#121212]/20 pt-2">
                    <div>VENUE: {evt.venue}</div>
                    <div>DATE: {dateFormatted} @ {timeFormatted}</div>
                  </div>
                </div>

                <div className="mt-5 pt-3 border-t-2 border-[#121212] flex items-center justify-between">
                  <div>
                    <div className="text-[9px] uppercase tracking-wider text-[#121212]/60">ADMISSION</div>
                    <div className="text-xs font-black text-[#121212]">₹250 / ₹500</div>
                  </div>

                  <Link
                    href={`/events/${evt.id}`}
                    className={`${accentBg} text-white text-xs font-black uppercase px-3 py-1.5 border-2 border-[#121212] shadow-[2px_2px_0px_#121212] hover:translate-x-0.5 hover:translate-y-0.5 transition`}
                  >
                    SELECT SEATS →
                  </Link>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
