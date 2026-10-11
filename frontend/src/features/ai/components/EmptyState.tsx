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
          <div className="flex h-20 w-20 items-center justify-center border-2 border-foreground bg-accent shadow-[4px_4px_0_#151515]">
            <Bot size={34} className="text-foreground" />
          </div>
        </div>

        <div className="mt-8 text-center">
          <div className="stamp inline-flex items-center gap-2 px-3 py-2 text-foreground">
            <Sparkles size={16} className="text-primary" />
            Guided scheme help
          </div>

          <h2 className="mt-5 text-4xl font-bold tracking-tight text-foreground">
            Ask a clearer question.
          </h2>

          <p className="mt-3 leading-7 text-text-muted">
            I can help you discover, compare and understand scheme information
            already available in JanSetu.
          </p>
        </div>

        <div className="mt-10">
          <h3 className="noticeboard-kicker mb-4 text-text-muted">
            Start with a task
          </h3>

          <div className="space-y-3">
            {prompts.map((prompt) => {
              const Icon = prompt.icon;
              return (
                <button
                  key={prompt.text}
                  onClick={() => onPromptClick(prompt.text)}
                  className="
                    flex w-full items-center gap-4 border-2 border-foreground bg-surface p-4 text-left shadow-[3px_3px_0_#151515]
                    transition-all hover:translate-x-px hover:translate-y-px hover:shadow-[2px_2px_0_#151515]
                  "
                >
                  <div className="border-2 border-foreground bg-accent p-2">
                    <Icon size={18} className="text-foreground" />
                  </div>
                  <span className="text-sm font-bold text-foreground">
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
