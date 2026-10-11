import type { ReactNode } from "react";
interface FormFieldProps {
  label: string;
  error?: string;
  required?: boolean;
  children: ReactNode;
}
export function FormField({
  label,
  error,
  required = false,
  children,
}: FormFieldProps) {
  return (
    <div className="space-y-2">
      <label className="block space-y-2">
        <span className="block text-sm font-medium">
          {label}
          {required && (
            <span aria-hidden="true" className="ml-1 text-destructive">
              *
            </span>
          )}
        </span>
        {children}
      </label>
      {error && (
        <p role="alert" className="text-sm text-destructive">
          {error}
        </p>
      )}
    </div>
  );
}
