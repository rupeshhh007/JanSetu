import {
  CheckCircle2,
  XCircle,
  Clock,
  Send,
  FileEdit,
  Ban,
  type LucideIcon,
} from "lucide-react";

import type { ApplicationStatus } from "../features/applications/types";

interface StatusMeta {
  label: string;
  icon: LucideIcon;
  badgeClassName: string;
  iconClassName: string;
}

export const APPLICATION_STATUS_META: Record<ApplicationStatus, StatusMeta> = {
  APPROVED: {
    label: "Approved",
    icon: CheckCircle2,
    badgeClassName: "bg-success text-success-foreground hover:bg-success",
    iconClassName: "bg-success/10 text-success",
  },
  REJECTED: {
    label: "Rejected",
    icon: XCircle,
    badgeClassName: "bg-destructive/10 text-destructive hover:bg-destructive/10",
    iconClassName: "bg-destructive/10 text-destructive",
  },
  UNDER_REVIEW: {
    label: "Under Review",
    icon: Clock,
    badgeClassName: "bg-info text-info-foreground hover:bg-info",
    iconClassName: "bg-info/10 text-info",
  },
  SUBMITTED: {
    label: "Submitted",
    icon: Send,
    badgeClassName: "bg-warning text-warning-foreground hover:bg-warning",
    iconClassName: "bg-warning/10 text-warning",
  },
  DRAFT: {
    label: "Draft",
    icon: FileEdit,
    badgeClassName: "bg-secondary text-secondary-foreground hover:bg-secondary",
    iconClassName: "bg-muted text-muted-foreground",
  },
  WITHDRAWN: {
    label: "Withdrawn",
    icon: Ban,
    badgeClassName: "bg-muted text-muted-foreground hover:bg-muted",
    iconClassName: "bg-muted text-muted-foreground",
  },
};
