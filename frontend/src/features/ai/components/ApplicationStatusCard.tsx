"use client";

import { Clock3, CheckCircle2, AlertCircle, FileText } from "lucide-react";

interface Props {
  content: string;
}

interface Application {
  scheme: string;
  status: string;
}

function parse(content: string): Application[] {
  const lines = content.split("\n");
  const apps: Application[] = [];
  let current: Partial<Application> = {};

  for (const line of lines) {
    if (line.startsWith("Scheme:")) {
      current.scheme = line.replace("Scheme:", "").trim();
    }
    if (line.startsWith("Status:")) {
      current.status = line.replace("Status:", "").trim();
      apps.push(current as Application);
      current = {};
    }
  }
  return apps;
}

export default function ApplicationStatusCard({ content }: Props) {
  const applications = parse(content);

  return (
    <div className="overflow-hidden rounded-2xl border border-zinc-800 bg-zinc-900">
      <div className="flex items-center gap-3 border-b border-zinc-800 bg-zinc-950 px-6 py-5">
        <div className="rounded-xl bg-zinc-800 p-2">
          <FileText size={18} className="text-zinc-300" />
        </div>
        <div>
          <p className="text-[15px] font-semibold text-zinc-100">
            Application Status
          </p>
        </div>
      </div>

      <div className="divide-y divide-zinc-800">
        {applications.map((app) => (
          <div
            key={app.scheme}
            className="flex items-center justify-between px-6 py-5"
          >
            <div>
              <h4 className="text-[15px] font-semibold text-zinc-100">
                {app.scheme}
              </h4>
            </div>

            <div>
              {app.status === "SUBMITTED" && (
                <div className="flex items-center gap-2 rounded-full border border-yellow-500/20 bg-yellow-500/10 px-3 py-1 text-[15px] font-medium text-yellow-500">
                  <Clock3 size={16} />
                  Submitted
                </div>
              )}

              {app.status === "APPROVED" && (
                <div className="flex items-center gap-2 rounded-full border border-green-500/20 bg-green-500/10 px-3 py-1 text-[15px] font-medium text-green-500">
                  <CheckCircle2 size={16} />
                  Approved
                </div>
              )}

              {app.status === "REJECTED" && (
                <div className="flex items-center gap-2 rounded-full border border-red-500/20 bg-red-500/10 px-3 py-1 text-[15px] font-medium text-red-500">
                  <AlertCircle size={16} />
                  Rejected
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}