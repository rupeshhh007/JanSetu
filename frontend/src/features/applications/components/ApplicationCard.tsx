import { FileText, CalendarClock, History, MessageSquare } from "lucide-react";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

import type { ApplicationResponse } from "../types";
import { ApplicationStatusBadge } from "./ApplicationStatusBadge";

interface Props {
  application: ApplicationResponse;
}

export function ApplicationCard({ application }: Props) {
  return (
    <Card className="group flex h-full flex-col border border-border transition-all duration-200 hover:-translate-y-0.5 hover:border-primary/30 hover:shadow-[var(--shadow-card-hover)]">
      <CardHeader className="flex flex-row items-start justify-between gap-3">
        <div className="flex items-start gap-3">
          <div className="flex size-10 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary transition-transform duration-200 group-hover:scale-105">
            <FileText className="size-5" strokeWidth={2} aria-hidden="true" />
          </div>
          <CardTitle className="pt-1 leading-snug">{application.schemeName}</CardTitle>
        </div>

        <ApplicationStatusBadge status={application.status} />
      </CardHeader>

      <CardContent className="space-y-4 text-sm">
        <div className="flex items-start gap-2.5">
          <MessageSquare className="mt-0.5 size-4 shrink-0 text-muted-foreground" aria-hidden="true" />
          <div>
            <p className="font-medium text-foreground">Remarks</p>
            <p className="text-muted-foreground">
              {application.remarks || "No remarks available."}
            </p>
          </div>
        </div>

        <div className="flex items-start gap-2.5">
          <CalendarClock className="mt-0.5 size-4 shrink-0 text-muted-foreground" aria-hidden="true" />
          <div>
            <p className="font-medium text-foreground">Submitted</p>
            <p className="text-muted-foreground">
              {new Date(application.submittedAt).toLocaleString()}
            </p>
          </div>
        </div>

        <div className="flex items-start gap-2.5">
          <History className="mt-0.5 size-4 shrink-0 text-muted-foreground" aria-hidden="true" />
          <div>
            <p className="font-medium text-foreground">Last Updated</p>
            <p className="text-muted-foreground">
              {new Date(application.updatedAt).toLocaleString()}
            </p>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
