import { Search } from "lucide-react";

import { Input } from "@/components/ui/input";

interface SearchBarProps {
  value: string;
  onChange: (value: string) => void;
}

export function SearchBar({ value, onChange }: SearchBarProps) {
  return (
    <div className="relative w-full max-w-xl">
      <Search
        className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground"
        aria-hidden="true"
      />

      <Input
        aria-label="Search schemes by name or description"
        placeholder="Search a scheme, benefit, or life situation"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="h-12 border-2 pl-10 shadow-[3px_3px_0_#151515]"
      />
    </div>
  );
}
