import Link from "next/link";
import { Navbar } from "@/components/layout/Navbar";
import { AICopilot } from "@/features/ai/components/AICopilot";
import { AuthGate } from "@/components/layout/AuthGate";
export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <AuthGate>
      <Navbar />
      <div className="workspace-main lg:ml-[248px]">
        <main
          id="main-content"
          className="workspace-content mx-auto min-h-screen max-w-[1440px] px-5 py-7 sm:px-9 lg:px-11"
        >
          <div className="no-print mb-8 flex items-center justify-between text-xs text-muted-foreground">
            <span>
              Workspace <span className="mx-2 text-border">/</span> JanSetu
            </span>
            <span className="flex items-center gap-2">
              <span className="size-1.5 rounded-full bg-primary" />
              Citizen workspace
            </span>
          </div>
          {children}
        </main>
        <footer className="no-print mx-5 border-t py-5 text-xs text-muted-foreground sm:mx-9 lg:mx-11">
          <div className="flex flex-wrap justify-between gap-3">
            <span>JanSetu is an independent discovery and preparation tool. Government applications must be completed through official channels.</span>
            <Link className="underline underline-offset-2 hover:text-foreground" href="/privacy">
              Privacy notice
            </Link>
          </div>
        </footer>
      </div>
      <AICopilot />
    </AuthGate>
  );
}
