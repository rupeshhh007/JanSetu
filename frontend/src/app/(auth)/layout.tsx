import { Landmark, ArrowUpRight } from "lucide-react";
import Link from "next/link";
export default function AuthLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <main
      id="main-content"
      className="grid min-h-screen lg:grid-cols-[1fr_1fr]"
    >
      <section className="relative hidden flex-col overflow-hidden bg-[#163d32] p-12 text-white lg:flex xl:p-16">
        <Link
          href="/login"
          className="flex items-center gap-3 text-4xl editorial"
        >
          <Landmark size={35} strokeWidth={1.3} />
          JanSetu
        </Link>
        <div className="relative z-10 my-auto max-w-lg py-20">
          <h2 className="editorial text-6xl leading-[1.1]">
            Public support.
            <br />
            <span className="text-[#b5d2be]">Personal clarity.</span>
          </h2>
          <p className="mt-7 max-w-sm text-base leading-8 text-white/70">
            Understand the schemes available to you. Prepare with confidence.
            Keep your next step in sight.
          </p>
          <div className="mt-9 flex items-center gap-3 text-sm text-white/90">
            <ArrowUpRight size={20} />
            Your bridge to a better-informed decision.
          </div>
        </div>
        <p className="relative z-10 border-t border-white/20 pt-6 text-xs text-white/60">
          An independent welfare discovery & preparation platform.
        </p>
        <div
          aria-hidden="true"
          className="absolute -bottom-48 -right-48 size-[580px] rounded-full border border-white/10 p-12"
        >
          <div className="h-full rounded-full border border-white/10 p-12">
            <div className="h-full rounded-full border border-white/10" />
          </div>
        </div>
      </section>
      <section className="flex items-center justify-center px-6 py-12 sm:px-12">
        <div className="w-full max-w-sm">
          <Link
            href="/login"
            className="mb-10 flex items-center gap-3 text-3xl text-primary editorial lg:hidden"
          >
            <Landmark size={30} />
            JanSetu
          </Link>
          {children}
          <p className="mt-8 text-xs leading-5 text-muted-foreground">
            Your profile helps explain recorded eligibility. JanSetu never asks
            you to upload identity documents.
          </p>
        </div>
      </section>
    </main>
  );
}
