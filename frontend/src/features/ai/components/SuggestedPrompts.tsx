"use client";

const prompts = [
  "Which schemes should I apply for first?",
  "Why am I eligible?",
  "Compare my top recommendations.",
  "What documents do I need?",
];

interface Props {
    prompts: string[];
    onSelect(prompt: string): void;
}

export function SuggestedPrompts({
  onSelect,
}: Props) {
  return (
    <div className="flex flex-wrap gap-2">
      {prompts.map((prompt) => (
        <button
          key={prompt}
          onClick={() => onSelect(prompt)}
          className="rounded-full border px-3 py-2 text-sm transition hover:bg-primary hover:text-white"
        >
          {prompt}
        </button>
      ))}
    </div>
  );
}