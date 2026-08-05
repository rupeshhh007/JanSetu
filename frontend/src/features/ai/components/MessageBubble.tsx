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
        "rounded-2xl p-5 shadow-sm leading-7 text-[15px]",
        isUser
          ? "ml-auto max-w-[85%] bg-zinc-100 text-zinc-900 border border-zinc-200 dark:bg-zinc-800 dark:text-zinc-100 dark:border-zinc-700"
          : "mr-auto max-w-[90%] bg-zinc-900 text-zinc-100 border border-zinc-800"
      )}
    >
      {children}
    </div>
  );
}