import { z } from "zod";

import type {
  Category,
  Gender,
  MaritalStatus,
  PrimaryOccupation,
} from "../types";

export const profileSchema = z.object({
  firstName: z
    .string()
    .trim()
    .min(1, "First name is required"),

  lastName: z
    .string()
    .trim()
    .min(1, "Last name is required"),

  dateOfBirth: z
    .string()
    .min(1, "Date of birth is required")
    .refine(
      (value) => new Date(value) < new Date(),
      "Date of birth must be in the past"
    ),

  gender: z.custom<Gender>(),

  state: z
    .string()
    .trim()
    .min(1, "State is required"),

  district: z
    .string()
    .trim()
    .min(1, "District is required"),

  pincode: z
    .string()
    .regex(
      /^[1-9][0-9]{5}$/,
      "Enter a valid 6-digit Indian pincode"
    ),

  category: z.custom<Category>(),

  annualFamilyIncome: z
    .number()
    .min(0, "Income cannot be negative"),

  maritalStatus: z.custom<MaritalStatus>(),

  primaryOccupation:
    z.custom<PrimaryOccupation>(),

  isStudent: z.boolean(),

  isFarmer: z.boolean(),

  hasDisability: z.boolean(),
});

export type ProfileFormValues = z.infer<
  typeof profileSchema
>;