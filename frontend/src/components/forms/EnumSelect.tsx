interface EnumSelectProps {
  value: string;
  placeholder: string;
  options: readonly string[];
  onChange: (value: string) => void;
}
export function EnumSelect({
  value,
  placeholder,
  options,
  onChange,
}: EnumSelectProps) {
  return (
    <select
      value={value}
      onChange={(event) => onChange(event.target.value)}
      className="h-11 w-full rounded-lg border border-input bg-white px-3 text-sm"
    >
      <option value="" disabled>
        {placeholder}
      </option>
      {options.map((option) => (
        <option key={option} value={option}>
          {option
            .toLowerCase()
            .split("_")
            .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
            .join(" ")}
        </option>
      ))}
    </select>
  );
}
