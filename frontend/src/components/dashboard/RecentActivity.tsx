import Link from "next/link";
import { ClipboardList, ArrowRight } from "lucide-react";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { formatRelativeTime } from "@/lib/utils";
import { APPLICATION_STATUS_META } from "@/lib/statusMeta";

import type { ApplicationResponse } from "@/features/applications/types";

interface RecentActivityProps {
  applications: ApplicationResponse[];
}

export function RecentActivity({ applications }: RecentActivityProps) {
  const recent = [...applications]
    .sort(
      (a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
    )
    .slice(0, 5);

  return (
    <Card className="border border-border">
      <CardHeader className="flex flex-row items-center justify-between border-b border-border pb-4">
        <CardTitle className="text-base">Recent activity</CardTitle>

        {applications.length > 0 && (
          <Link
            href="/applications"
            className="flex items-center gap-1 text-sm font-medium text-primary hover:underline"
          >
            View all
            <ArrowRight className="size-3.5" aria-hidden="true" />
          </Link>
        )}
      </CardHeader>

      <CardContent className="pt-2">
        {recent.length === 0 ? (
          <div className="flex flex-col items-center gap-3 py-10 text-center">
            <div className="flex size-11 items-center justify-center rounded-full bg-muted">
              <ClipboardList className="size-5 text-muted-foreground" aria-hidden="true" />
            </div>
            <p className="text-sm text-muted-foreground">
              No activity yet. Applications you submit will show up here.
            </p>
          </div>
        ) : (
          <ul className="divide-y divide-border">
            {recent.map((application) => {
              const meta = APPLICATION_STATUS_META[application.status];
              const Icon = meta.icon;

              return (
                <li
                  key={application.applicationId}
                  className="flex items-center gap-4 py-3 first:pt-0 last:pb-0"
                >
                  <div
                    className={`flex size-9 shrink-0 items-center justify-center rounded-lg ${meta.iconClassName}`}
                  >
                    <Icon className="size-4" aria-hidden="true" />
                  </div>

                  <div className="min-w-0 flex-1">
                    <p className="truncate text-sm font-medium text-foreground">
                      {application.schemeName}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {meta.label} · {formatRelativeTime(application.updatedAt)}
                    </p>
                  </div>
                </li>
              );
            })}
          </ul>
        )}
      </CardContent>
    </Card>
  );
}
