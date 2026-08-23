import { Sparkles } from "lucide-react";

export default function AIAvatar() {
  return (
    <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-primary text-primary-foreground shadow-sm">
      <Sparkles size={18} />
    </div>
  );
}