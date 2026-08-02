import { Badge } from "@/components/ui/badge";
import { APPLICATION_STATUS_META } from "@/lib/statusMeta";

import type { ApplicationStatus } from "../types";

interface Props {
  status: ApplicationStatus;
}

export function ApplicationStatusBadge({ status }: Props) {
  const meta = APPLICATION_STATUS_META[status];
  const Icon = meta.icon;

  return (
    <Badge className={meta.badgeClassName}>
      <Icon className="size-3" aria-hidden="true" />
      {meta.label}
    </Badge>
  );
}
