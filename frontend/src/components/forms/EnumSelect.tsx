import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

interface EnumSelectProps {
  value: string;
  placeholder: string;
  options: readonly string[];
  onChange: (value: string) => void;
}

function formatLabel(value: string) {
  return value
    .toLowerCase()
    .split("_")
    .map(
      (word) => word.charAt(0).toUpperCase() + word.slice(1)
    )
    .join(" ");
}

export function EnumSelect({
  value,
  placeholder,
  options,
  onChange,
}: EnumSelectProps) {
  return (
    <Select
      value={value}
      onValueChange={(value) => {
        if (value !== null) {
          onChange(value);
        }
      }}
    >
      <SelectTrigger className="w-full">
        <SelectValue placeholder={placeholder} />
      </SelectTrigger>

      <SelectContent>
        {options.map((option) => (
          <SelectItem
            key={option}
            value={option}
          >
            {formatLabel(option)}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}