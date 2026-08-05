// src/features/ai/components/EmptyState.tsx
"use client";

import {
  Bot,
  Sparkles,
  FileText,
  ShieldCheck,
  Landmark,
  ArrowRightLeft,
} from "lucide-react";

interface Props {
  onPromptClick: (prompt: string) => void;
}

const prompts = [
  {
    icon: Landmark,
    text: "Which schemes should I apply for?",
  },
  {
    icon: ArrowRightLeft,
    text: "Compare PM-KISAN and Ayushman Bharat PM-JAY",
  },
  {
    icon: FileText,
    text: "What is my application status?",
  },
  {
    icon: ShieldCheck,
    text: "Explain why I'm eligible for PM-KISAN",
  },
];

export default function EmptyState({ onPromptClick }: Props) {
  return (
    <div className="flex h-full flex-col justify-center">
      <div className="mx-auto w-full max-w-md">
        <div className="flex justify-center">
          <div className="flex h-20 w-20 items-center justify-center rounded-3xl bg-zinc-900 border border-zinc-800 shadow-lg">
            <Bot size={34} className="text-zinc-100" />
          </div>
        </div>

        <div className="mt-8 text-center">
          <div className="inline-flex items-center gap-2 rounded-full bg-zinc-900 border border-zinc-800 px-4 py-2 text-sm font-medium text-zinc-300">
            <Sparkles size={16} className="text-zinc-400" />
            Powered by AI
          </div>

          <h2 className="mt-5 text-4xl font-bold tracking-tight text-zinc-100">
            JanSetu AI
          </h2>

          <p className="mt-3 leading-7 text-zinc-400">
            Your intelligent assistant for discovering, comparing and understanding Government Welfare Schemes.
          </p>
        </div>

        <div className="mt-10">
          <h3 className="mb-4 text-sm font-semibold uppercase tracking-widest text-zinc-500">
            Popular Questions
          </h3>

          <div className="space-y-3">
            {prompts.map((prompt) => {
              const Icon = prompt.icon;
              return (
                <button
                  key={prompt.text}
                  onClick={() => onPromptClick(prompt.text)}
                  className="
                    flex w-full items-center gap-4 rounded-xl border border-zinc-800 bg-zinc-900 p-4 text-left shadow-sm 
                    transition-all hover:-translate-y-1 hover:border-zinc-600 hover:bg-zinc-800 hover:shadow-xl
                  "
                >
                  <div className="rounded-xl bg-zinc-800 p-2">
                    <Icon size={18} className="text-zinc-300" />
                  </div>
                  <span className="text-sm font-medium text-zinc-100">
                    {prompt.text}
                  </span>
                </button>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}