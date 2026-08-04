import type { LucideIcon } from "lucide-react";
import {
  Calendar,
  Wallet,
  Users,
  Briefcase,
  GraduationCap,
  Sprout,
  Accessibility,
  Globe,
} from "lucide-react";

import type { EligibilityCriteria } from "../types";

interface EligibilityBadgesProps {
  eligibility: EligibilityCriteria | null | undefined;
}

function formatEnum(value: string) {
  return value
    .toLowerCase()
    .replace(/_/g, " ")
    .replace(/\b\w/g, (char) => char.toUpperCase());
}

interface Chip {
  key: string;
  icon: LucideIcon;
  label: string;
}

/**
 * Renders only the eligibility criteria that are actually set on the scheme.
 * Every chip maps 1:1 to a non-null field already returned by the API —
 * nothing is invented. A criterion left null by the backend means "no
 * restriction", so it correctly produces no chip. Flags that are `false`
 * (not just null) are treated the same way, since "not required" isn't
 * something a citizen needs a badge to know.
 */
export function EligibilityBadges({ eligibility }: EligibilityBadgesProps) {
  const criteria = eligibility ?? ({} as EligibilityCriteria);
  const chips: Chip[] = [];

  if (criteria.minAge != null || criteria.maxAge != null) {
    const label =
      criteria.minAge != null && criteria.maxAge != null
        ? `Age ${criteria.minAge}\u2013${criteria.maxAge}`
        : criteria.minAge != null
          ? `Age ${criteria.minAge}+`
          : `Up to ${criteria.maxAge} yrs`;
    chips.push({ key: "age", icon: Calendar, label });
  }

  if (criteria.maxFamilyIncome != null) {
    chips.push({
      key: "income",
      icon: Wallet,
      label: `Income \u2264 \u20b9${criteria.maxFamilyIncome.toLocaleString("en-IN")}`,
    });
  }

  (criteria.requiredCategories ?? []).forEach((category) => {
    chips.push({ key: `cat-${category}`, icon: Users, label: formatEnum(category) });
  });

  (criteria.allowedOccupations ?? []).forEach((occupation) => {
    chips.push({ key: `occ-${occupation}`, icon: Briefcase, label: formatEnum(occupation) });
  });

  if (criteria.requiresStudent) {
    chips.push({ key: "student", icon: GraduationCap, label: "Students" });
  }

  if (criteria.requiresFarmer) {
    chips.push({ key: "farmer", icon: Sprout, label: "Farmers" });
  }

  if (criteria.requiresDisability) {
    chips.push({ key: "disability", icon: Accessibility, label: "Persons with Disability" });
  }

  if (chips.length === 0) {
    chips.push({ key: "open", icon: Globe, label: "Open to all citizens" });
  }

  return (
    <ul className="flex flex-wrap gap-1.5" aria-label="Eligibility criteria">
      {chips.map((chip) => (
        <li
          key={chip.key}
          className="inline-flex items-center gap-1.5 rounded-full border border-border bg-muted/50 px-2.5 py-1 text-xs font-medium text-foreground"
        >
          <chip.icon className="size-3.5 text-muted-foreground" aria-hidden="true" />
          {chip.label}
        </li>
      ))}
    </ul>
  );
}
