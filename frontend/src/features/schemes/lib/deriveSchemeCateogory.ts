import type { LucideIcon } from "lucide-react";
import {
  Sprout,
  GraduationCap,
  HeartPulse,
  Briefcase,
  Home,
  Users,
  UserRound,
  Accessibility,
  Wallet,
  Landmark,
} from "lucide-react";

export interface SchemeCategory {
  label: string;
  icon: LucideIcon;
  accent: "primary" | "success" | "neutral";
}

interface CategoryRule {
  label: string;
  icon: LucideIcon;
  accent: SchemeCategory["accent"];
  keywords: string[];
}

/**
 * The backend does NOT return a "category" field on SchemeResponse — only
 * raw eligibility criteria. Per product direction, this function derives a
 * *visual-only* grouping purely from the scheme name so cards can show a
 * category badge for scannability.
 *
 * This is a display heuristic, not authoritative data. It never invents,
 * overrides, or supplements any real field used by matching, eligibility,
 * or application logic — those all continue to read straight from the API.
 */
const CATEGORY_RULES: CategoryRule[] = [
  {
    label: "Agriculture",
    icon: Sprout,
    accent: "success",
    keywords: ["farmer", "farming", "agricult", "crop", "kisan", "krishi", "soil"],
  },
  {
    label: "Education",
    icon: GraduationCap,
    accent: "primary",
    keywords: ["student", "education", "scholarship", "school", "college", "shiksha", "vidya"],
  },
  {
    label: "Health",
    icon: HeartPulse,
    accent: "success",
    keywords: ["health", "medical", "hospital", "ayushman", "treatment"],
  },
  {
    label: "Housing",
    icon: Home,
    accent: "neutral",
    keywords: ["housing", "awas", "home", "ghar"],
  },
  {
    label: "Women & Child",
    icon: Users,
    accent: "success",
    keywords: ["women", "mahila", "girl", "child", "bal", "matru", "maternity"],
  },
  {
    label: "Senior Citizen",
    icon: UserRound,
    accent: "neutral",
    keywords: ["pension", "senior", "elderly", "vridha", "old age"],
  },
  {
    label: "Accessibility",
    icon: Accessibility,
    accent: "primary",
    keywords: ["disab", "divyang", "accessib"],
  },
  {
    label: "Employment",
    icon: Briefcase,
    accent: "primary",
    keywords: ["employ", "business", "startup", "entrepreneur", "rojgar", "udyam", "self-employ"],
  },
  {
    label: "Financial Inclusion",
    icon: Wallet,
    accent: "success",
    keywords: ["bank", "jan dhan", "saving", "subsidy", "financial", "loan"],
  },
];

const FALLBACK_CATEGORY: SchemeCategory = {
  label: "Welfare Scheme",
  icon: Landmark,
  accent: "neutral",
};

export function deriveSchemeCategory(schemeName: string): SchemeCategory {
  const normalized = schemeName.toLowerCase();

  const match = CATEGORY_RULES.find((rule) =>
    rule.keywords.some((keyword) => normalized.includes(keyword))
  );

  if (!match) return FALLBACK_CATEGORY;

  return { label: match.label, icon: match.icon, accent: match.accent };
}
