import Link from "next/link";
import { ClipboardList } from "lucide-react";
import { formatRelativeTime } from "@/lib/utils";
import { APPLICATION_STATUS_META } from "@/lib/statusMeta";
import type { ApplicationResponse } from "@/features/applications/types";

interface RecentActivityProps {
  applications: ApplicationResponse[];
}

export function RecentActivity({ applications }: RecentActivityProps) {
  const recent = [...applications]
    .sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
    .slice(0, 5);

  return (
    <div className="flex flex-col gap-4 h-full">
      <div className="flex items-center justify-between pl-2 pr-4">
        <h2 className="text-sm font-semibold tracking-wide text-foreground/80">
          Recent Activity
        </h2>
        {applications.length > 0 && (
          <Link href="/applications" className="text-sm font-medium text-primary hover:underline">
            View all
          </Link>
        )}
      </div>

      <div className="flex-1 rounded-[2rem] bg-white/60 backdrop-blur-xl border border-white/60 shadow-[0_8px_30px_rgb(0,0,0,0.04)] overflow-hidden">
        {recent.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full gap-4 py-16 text-center">
            <div className="flex size-16 items-center justify-center rounded-full bg-black/5 text-muted-foreground">
              <ClipboardList className="size-8" aria-hidden="true" />
            </div>
            <p className="text-[15px] text-muted-foreground font-medium">
              No activity yet.
            </p>
          </div>
        ) : (
          <div className="flex flex-col py-2">
            {recent.map((application, index) => {
              const meta = APPLICATION_STATUS_META[application.status];
              const Icon = meta.icon;
              const isLast = index === recent.length - 1;

              return (
                <div key={application.applicationId} className="relative group">
                  <div className="flex items-center gap-4 px-6 py-4 transition-colors hover:bg-black/[0.02]">
                    <div className={`flex size-12 shrink-0 items-center justify-center rounded-2xl ${meta.iconClassName} shadow-sm`}>
                      <Icon className="size-6" aria-hidden="true" />
                    </div>
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-[17px] font-semibold text-foreground tracking-tight">
                        {application.schemeName}
                      </p>
                      <p className="text-[14px] font-medium text-muted-foreground mt-0.5">
                        {meta.label} <span className="mx-1.5 opacity-50">•</span> {formatRelativeTime(application.updatedAt)}
                      </p>
                    </div>
                  </div>
                  {!isLast && (
                    <div className="absolute bottom-0 left-[5.5rem] right-0 h-px bg-black/5" />
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
