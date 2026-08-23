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
        "leading-relaxed",
        isUser
          ? "ml-auto max-w-[85%] bg-secondary/80 text-foreground rounded-[1.5rem] px-6 py-3.5 font-medium text-[15px]"
          : "mr-auto w-full bg-transparent text-foreground text-[16px]"
      )}
    >
      {children}
    </div>
  );
}