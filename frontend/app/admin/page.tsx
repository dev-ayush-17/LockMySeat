'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Event, apiRequest, getStoredUser } from '@/lib/api';

export default function AdminPage() {
  const router = useRouter();
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Form states
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [city, setCity] = useState('');
  const [venue, setVenue] = useState('');
  const [eventDate, setEventDate] = useState('');

  const checkAdminAndLoad = async () => {
    const user = getStoredUser();
    if (!user || user.role !== 'ADMIN') {
      router.push('/login');
      return;
    }

    try {
      const data = await apiRequest<Event[]>('/events');
      setEvents(data);
    } catch (err: any) {
      setError(err.message || 'FAILED TO LOAD MANIFEST.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    checkAdminAndLoad();
  }, []);

  const handleCreateEvent = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    setSuccess(null);

    try {
      const isoDate = new Date(eventDate).toISOString();
      await apiRequest<Event>('/events', {
        method: 'POST',
        body: JSON.stringify({
          title,
          description,
          city,
          venue,
          eventDate: isoDate,
        }),
      });

      setSuccess('EVENT GENERATED WITH 30-SEAT TOPOLOGY (ROWS A-B VIP @ ₹500, C-E REGULAR @ ₹250)');
      setTitle('');
      setDescription('');
      setCity('');
      setVenue('');
      setEventDate('');
      await checkAdminAndLoad();
    } catch (err: any) {
      setError(err.message || 'CREATION FAILED.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteEvent = async (id: number) => {
    if (!confirm('CASCADE DELETION: Remove this event and eliminate all associated seat entities?')) {
      return;
    }

    setDeleteId(id);
    setError(null);
    setSuccess(null);

    try {
      await apiRequest<{ message: string }>(`/events/${id}`, {
        method: 'DELETE',
      });
      setSuccess('EVENT & ASSOCIATED SEATS PURGED.');
      await checkAdminAndLoad();
    } catch (err: any) {
      setError(err.message || 'DELETE OPERATION FAILED.');
    } finally {
      setDeleteId(null);
    }
  };

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-20 text-center font-mono">
        <div className="inline-block p-3 border-2 border-[#121212] bg-[#EBE4D5] shadow-[4px_4px_0px_#121212]">
          VERIFYING_ADMIN_CREDENTIALS...
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 py-10 font-mono">
      <div className="mb-8 border-b-2 border-[#121212] pb-4">
        <div className="inline-block bg-[#E5A93C] text-[#121212] border-2 border-[#121212] shadow-[2px_2px_0px_#121212] text-[10px] font-black uppercase px-2 py-0.5 mb-2">
          ADMIN_CONSOLE // ROOT_AUTHORITY
        </div>
        <h1 className="text-2xl sm:text-3xl font-black text-[#121212] uppercase tracking-tight">EVENT_TOPOLOGY_MANAGER</h1>
        <p className="text-xs text-[#121212]/70 font-sans mt-1">
          Direct system administration: inject new event schedules or cascade delete out-of-scope performances.
        </p>
      </div>

      {error && (
        <div className="mb-6 p-3 border-2 border-[#D9381E] bg-[#D9381E]/10 text-[#D9381E] text-xs font-bold uppercase">
          {error}
        </div>
      )}

      {success && (
        <div className="mb-6 p-3 border-2 border-[#121212] bg-[#E5A93C] text-[#121212] text-xs font-black uppercase shadow-[3px_3px_0px_#121212]">
          {success}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left: Create Event Form */}
        <div className="border-2 border-[#121212] bg-[#EBE4D5] p-6 shadow-[5px_5px_0px_#121212] h-fit">
          <div className="border-b-2 border-[#121212] pb-2 mb-4">
            <h2 className="text-sm font-black uppercase text-[#121212]">PUBLISH_EVENT</h2>
          </div>

          <form onSubmit={handleCreateEvent} className="space-y-3.5">
            <div>
              <label className="block text-[10px] font-bold uppercase text-[#121212] mb-1">
                EVENT TITLE
              </label>
              <input
                type="text"
                required
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="e.g. Vintage Jazz Night"
                className="w-full bg-[#F4EFE6] border-2 border-[#121212] px-2.5 py-1.5 text-xs text-[#121212] placeholder-[#121212]/40 focus:outline-none focus:bg-white font-mono"
              />
            </div>

            <div>
              <label className="block text-[10px] font-bold uppercase text-[#121212] mb-1">
                DESCRIPTION
              </label>
              <textarea
                rows={2}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Synopsis of performance..."
                className="w-full bg-[#F4EFE6] border-2 border-[#121212] px-2.5 py-1.5 text-xs text-[#121212] placeholder-[#121212]/40 focus:outline-none focus:bg-white font-mono"
              />
            </div>

            <div className="grid grid-cols-2 gap-2">
              <div>
                <label className="block text-[10px] font-bold uppercase text-[#121212] mb-1">
                  CITY
                </label>
                <input
                  type="text"
                  required
                  value={city}
                  onChange={(e) => setCity(e.target.value)}
                  placeholder="e.g. Pune"
                  className="w-full bg-[#F4EFE6] border-2 border-[#121212] px-2.5 py-1.5 text-xs text-[#121212] placeholder-[#121212]/40 focus:outline-none focus:bg-white font-mono"
                />
              </div>

              <div>
                <label className="block text-[10px] font-bold uppercase text-[#121212] mb-1">
                  VENUE
                </label>
                <input
                  type="text"
                  required
                  value={venue}
                  onChange={(e) => setVenue(e.target.value)}
                  placeholder="e.g. Bal Gandharva"
                  className="w-full bg-[#F4EFE6] border-2 border-[#121212] px-2.5 py-1.5 text-xs text-[#121212] placeholder-[#121212]/40 focus:outline-none focus:bg-white font-mono"
                />
              </div>
            </div>

            <div>
              <label className="block text-[10px] font-bold uppercase text-[#121212] mb-1">
                EVENT TIMESTAMP
              </label>
              <input
                type="datetime-local"
                required
                value={eventDate}
                onChange={(e) => setEventDate(e.target.value)}
                className="w-full bg-[#F4EFE6] border-2 border-[#121212] px-2.5 py-1.5 text-xs text-[#121212] focus:outline-none focus:bg-white font-mono"
              />
            </div>

            <div className="p-2 border border-[#121212] bg-[#F4EFE6] text-[10px] text-[#121212]/80">
              ⚡ <strong>AUTOGEN GUARANTEE:</strong> Instantly provisions 30 physical seat records with OOP polymorphism.
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="w-full bg-[#121212] hover:bg-[#D9381E] text-white font-black uppercase py-2.5 border-2 border-[#121212] shadow-[3px_3px_0px_#121212] text-xs transition disabled:opacity-40"
            >
              {submitting ? 'GENERATING...' : 'PUBLISH EVENT & SEATS'}
            </button>
          </form>
        </div>

        {/* Right: Existing Events Table */}
        <div className="lg:col-span-2 border-2 border-[#121212] bg-[#EBE4D5] p-6 shadow-[5px_5px_0px_#121212]">
          <div className="border-b-2 border-[#121212] pb-2 mb-4">
            <h2 className="text-sm font-black uppercase text-[#121212]">ACTIVE_INVENTORY ({events.length})</h2>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="text-[10px] uppercase text-[#121212] border-b-2 border-[#121212]">
                <tr>
                  <th className="py-2 px-2.5">TITLE // VENUE</th>
                  <th className="py-2 px-2.5">DATE</th>
                  <th className="py-2 px-2.5">SEATS</th>
                  <th className="py-2 px-2.5 text-right">ACTION</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#121212]/20 text-[#121212]">
                {events.map((evt) => (
                  <tr key={evt.id} className="hover:bg-[#F4EFE6]/70">
                    <td className="py-2.5 px-2.5">
                      <div className="font-bold">{evt.title}</div>
                      <div className="text-[10px] text-[#121212]/60">LOC: {evt.city} • {evt.venue}</div>
                    </td>
                    <td className="py-2.5 px-2.5 whitespace-nowrap text-[11px]">
                      {new Date(evt.eventDate).toLocaleDateString('en-IN', {
                        day: '2-digit',
                        month: 'short',
                        year: 'numeric',
                      })}
                    </td>
                    <td className="py-2.5 px-2.5 font-bold">
                      {evt.availableSeats} / {evt.totalSeats}
                    </td>
                    <td className="py-2.5 px-2.5 text-right whitespace-nowrap">
                      <button
                        onClick={() => handleDeleteEvent(evt.id)}
                        disabled={deleteId === evt.id}
                        className="bg-[#D9381E] hover:bg-[#121212] text-white px-2 py-1 text-[10px] font-black uppercase border border-[#121212] transition disabled:opacity-40"
                      >
                        {deleteId === evt.id ? '...' : 'PURGE'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
