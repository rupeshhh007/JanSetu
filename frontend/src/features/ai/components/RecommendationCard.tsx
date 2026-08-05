"use client";

import { ArrowRight, BadgeCheck, Sparkles, Star } from "lucide-react";

interface RecommendationCardProps {
  content: string;
}

function extractTitle(text: string) {
  const lines = text.split("\n");
  for (const line of lines) {
    if (line.includes("**") && !line.toLowerCase().includes("recommend")) {
      return line.replace(/\*/g, "").trim();
    }
  }
  return "Recommended Scheme";
}

function extractBullets(text: string) {
  return text
    .split("\n")
    .filter(
      (line) => line.trim().startsWith("*") || line.trim().startsWith("-")
    )
    .map((line) => line.replace(/^\*|-/, "").trim());
}

export default function RecommendationCard({ content }: RecommendationCardProps) {
  const title = extractTitle(content);
  const bullets = extractBullets(content);

  return (
    <div className="overflow-hidden rounded-2xl border border-zinc-800 bg-zinc-900 shadow-sm">
      {/* Header */}
      <div className="flex items-center gap-3 border-b border-zinc-800 bg-zinc-950 px-6 py-5">
        <div className="rounded-xl bg-zinc-800 p-2">
          <Sparkles className="text-zinc-300" size={18} />
        </div>
        <div>
          <p className="text-xs font-medium uppercase tracking-widest text-zinc-400">
            AI Recommendation
          </p>
          <h3 className="text-[15px] font-semibold text-zinc-100">{title}</h3>
        </div>
      </div>

      {/* Rating */}
      <div className="flex items-center gap-2 px-6 pt-5">
        <Star size={16} fill="currentColor" className="text-zinc-300" />
        <span className="text-[15px] font-medium text-zinc-400">Top Match</span>
      </div>

      {/* Reasons */}
      <div className="space-y-4 px-6 py-5">
        {bullets.map((bullet, index) => (
          <div key={index} className="flex items-start gap-3">
            <BadgeCheck size={18} className="mt-1 shrink-0 text-zinc-300" />
            <p className="text-[15px] leading-7 text-zinc-300">{bullet}</p>
          </div>
        ))}
      </div>

      {/* Footer */}
      <div className="flex items-center justify-between border-t border-zinc-800 bg-zinc-950 px-6 py-5">
        <span className="text-xs text-zinc-500">
          Personalized recommendation
        </span>
        <button
          className="
          flex items-center gap-2 rounded-xl bg-white px-4 py-2 text-[15px] font-medium text-black transition hover:bg-zinc-200
        "
        >
          Learn More <ArrowRight size={16} />
        </button>
      </div>
    </div>
  );
}