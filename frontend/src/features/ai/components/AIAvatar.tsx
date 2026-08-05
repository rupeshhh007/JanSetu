import { Bot } from "lucide-react";

export default function AIAvatar() {
  return (
    <div className="flex h-10 w-10 items-center justify-center rounded-full border border-zinc-800 bg-black text-white shadow-md">
      <Bot size={18} />
    </div>
  );
}