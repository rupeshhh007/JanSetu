import { cn } from "@/lib/utils";

interface Props {
  children: React.ReactNode;
  role: "user" | "assistant";
}

export default function MessageBubble({ children, role }: Props) {
  const isUser = role === "user";

  return (
    <div
      className={cn(
        "rounded-2xl px-5 py-3.5 shadow-sm leading-relaxed text-[15px]",
        isUser
          ? "ml-auto max-w-[85%] bg-primary text-white border-0 rounded-br-sm"
          : "mr-auto max-w-[90%] bg-muted text-foreground border border-black/5 rounded-bl-sm"
      )}
    >
      {children}
    </div>
  );
}