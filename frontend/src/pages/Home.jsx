export default function LandingPage() {
  const stats = [
    {
      value: '94%',
      label: 'Match accuracy vs recruiter review',
    },
    {
      value: '12s',
      label: 'Average analysis time',
    },
    {
      value: '3.2x',
      label: 'More callbacks reported',
    },
    {
      value: 'Free',
      label: 'To start, no card needed',
    },
  ];

  return (
    <div className="font-space-grotesk relative flex h-screen overflow-hidden bg-[#071915] text-white">
      {/* Grid Background */}
      <div
        className="absolute inset-0 opacity-[0.06]"
        style={{
          backgroundImage: `
            linear-gradient(rgba(90,255,200,0.12) 1px, transparent 1px),
            linear-gradient(90deg, rgba(90,255,200,0.12) 1px, transparent 1px)
          `,
          backgroundSize: '40px 40px',
        }}
      />

      {/* Glow */}
      <div className="absolute -top-52 -left-52 h-125 w-125 rounded-full bg-emerald-400/10 blur-[180px]" />

      <div className="relative z-10 flex w-full flex-col px-10 py-8">
        {/* Header */}
        <header className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#67FFD3]">
              <svg
                width="14"
                height="14"
                viewBox="0 0 24 24"
                fill="none"
                className="text-black"
              >
                <path
                  d="M8 5L16 12L8 19"
                  stroke="currentColor"
                  strokeWidth="3"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
              </svg>
            </div>

            <span className="text-lg font-semibold">ResumeAI</span>
          </div>

          <button className="group flex cursor-pointer items-center gap-2 rounded-xl bg-[#67FFD3] px-5 py-2.5 text-sm font-semibold text-black transition-all duration-300 hover:scale-105 hover:bg-[#7fffdc]">
            Get Started
            <span className="transition-transform duration-300 group-hover:translate-x-1">
              →
            </span>
          </button>
        </header>

        {/* Main Content */}
        <main className="flex flex-1 flex-col justify-center">
          {/* Hero */}
          <section className="max-w-4xl">
            <p className="text-xs font-medium tracking-[0.4em] text-[#52D9B6] uppercase">
              AI-Powered Analysis
            </p>

            <h1 className="mt-5 text-[68px] leading-[0.9] font-black tracking-[-0.04em] xl:text-[76px]">
              Land the role
              <br />
              you <span className="text-[#67FFD3]">actually</span>
              <br />
              want.
            </h1>

            <p className="mt-6 max-w-4xl text-base leading-relaxed text-gray-500 xl:text-lg">
              Upload your resume, paste any job description, and our AI tells
              you exactly where you stand — skills matched, gaps flagged,
              rewrites suggested.
            </p>
          </section>

          {/* Stats Cards */}
          <section className="mt-10 grid max-w-6xl grid-cols-2 gap-4">
            {stats.map((item) => (
              <div
                key={item.value}
                className="rounded-2xl border border-[#1d3a34] bg-[#0b201c]/70 p-6 backdrop-blur-sm"
              >
                <h3 className="text-4xl font-black text-[#67FFD3]">
                  {item.value}
                </h3>

                <p className="mt-2 text-sm text-gray-500">{item.label}</p>
              </div>
            ))}
          </section>
        </main>

        {/* Footer */}
        <footer className="border-t border-[#14302b] pt-6">
          <div className="flex items-center gap-4">
            <div className="flex -space-x-2">
              <div className="flex h-8 w-8 items-center justify-center rounded-full border-2 border-[#071915] bg-[#d9fff3] text-[10px] font-bold text-black">
                AC {/* Hard coded need to change */}
              </div>

              <div className="flex h-8 w-8 items-center justify-center rounded-full border-2 border-[#071915] bg-[#bffce9] text-[10px] font-bold text-black">
                SR {/* Hard coded need to change */}
              </div>

              <div className="flex h-8 w-8 items-center justify-center rounded-full border-2 border-[#071915] bg-[#a0f6dd] text-[10px] font-bold text-black">
                MK {/* Hard coded need to change */}
              </div>
            </div>

            <div>
              <p className="text-sm text-gray-500">
                Joined by{' '}
                <span className="font-semibold text-[#67FFD3]">
                  2,400+ candidates {/* Hard coded need to change */}
                </span>
              </p>

              <p className="text-sm text-gray-500">in the last 30 days</p>
            </div>
          </div>
        </footer>
      </div>
    </div>
  );
}
