"use client";

import React from "react";
import { ArrowRightLeft, CheckCircle2, Trophy } from "lucide-react";

interface Props {
  content: string;
}

interface Scheme {
  name: string;
  description: string;
  eligibility: string[];
}

interface ParsedData {
  schemes: Scheme[];
  recommendation: string;
}

function parse(content: string): ParsedData {
  const lines = content.split("\n");
  const schemes: Scheme[] = [];
  let current: Partial<Scheme> | null = null;
  let recommendation = "";
  let mode: "INTRO" | "SCHEME" | "RECOMMENDATION" = "INTRO";

  // Keywords to ignore so we don't treat meta-headers as schemes
  const ignoreKeywords = ["comparison", "overview", "summary", "vs", "versus"];

  for (const line of lines) {
    const clean = line.trim();
    if (!clean) continue;

    // Detect Markdown Headers e.g., **PM-KISAN**
    if (clean.startsWith("**") && clean.endsWith("**")) {
      const header = clean.replace(/\*/g, "").trim();
      const lower = header.toLowerCase();

      // 1. Is it a Recommendation header?
      if (lower.includes("recommendation") || lower.includes("conclusion")) {
        mode = "RECOMMENDATION";
        if (current && current.name) {
          schemes.push(current as Scheme);
          current = null;
        }
        continue;
      }

      // 2. Is it a generic Intro/Comparison header?
      if (ignoreKeywords.some((kw) => lower.includes(kw))) {
        mode = "INTRO";
        continue;
      }

      // 3. It's an actual Scheme!
      mode = "SCHEME";
      if (current && current.name) {
        schemes.push(current as Scheme);
      }
      current = { name: header, description: "", eligibility: [] };
      continue;
    }

    // Parse content based on our current mode
    if (mode === "SCHEME" && current) {
      if (clean.startsWith("*") || clean.startsWith("-")) {
        // It's a bullet point
        current.eligibility!.push(
          clean.replace(/^\*|-/, "").replace(/\*\*/g, "").trim()
        );
      } else if (current.eligibility!.length === 0) {
        // It's description text (only append if we haven't started listing bullets yet)
        current.description = current.description
          ? current.description + " " + clean.replace(/\*\*/g, "")
          : clean.replace(/\*\*/g, "");
      }
    } else if (mode === "RECOMMENDATION") {
      // Collect all text under the recommendation block
      recommendation +=
        (recommendation ? "\n" : "") + clean.replace(/\*\*/g, "");
    }
  }

  // Push the final scheme if exists
  if (current && current.name) {
    schemes.push(current as Scheme);
  }

  return { schemes, recommendation };
}

export default function ComparisonCard({ content }: Props) {
  const { schemes, recommendation } = parse(content);

  // Fallback: If parsing fails to find 2 schemes, render beautifully formatted text
  if (schemes.length < 2) {
    return (
      <div className="rounded-2xl border border-zinc-800 bg-zinc-900 p-5 shadow-sm">
        <pre className="whitespace-pre-wrap font-sans text-[15px] leading-7 text-zinc-300">
          {content}
        </pre>
      </div>
    );
  }

  return (
    <div className="overflow-hidden rounded-2xl border border-zinc-800 bg-zinc-950 shadow-sm">
      {/* Header */}
      <div className="flex items-center gap-3 border-b border-zinc-800 bg-zinc-950 px-6 py-5">
        <div className="rounded-xl bg-zinc-800 p-2">
          <ArrowRightLeft className="text-zinc-300" size={18} />
        </div>
        <div>
          <p className="text-xs font-medium uppercase tracking-widest text-zinc-400">
            AI Comparison
          </p>
          <h3 className="text-[15px] font-semibold text-zinc-100">
            Compare Schemes
          </h3>
        </div>
      </div>

      {/* Body: Stacked Schemes */}
      <div className="flex flex-col p-5">
        <div className="flex flex-col space-y-4">
          {schemes.map((scheme, index) => (
            <React.Fragment key={scheme.name}>
              {/* Individual Scheme Card */}
              <div className="rounded-xl border border-zinc-800 bg-zinc-900 p-5 shadow-sm">
                <h4 className="text-[15px] font-semibold text-zinc-100">
                  {scheme.name}
                </h4>
                
                {scheme.description && (
                  <p className="mt-3 text-[15px] leading-7 text-zinc-400">
                    {scheme.description}
                  </p>
                )}

                {scheme.eligibility.length > 0 && (
                  <div className="mt-5 space-y-3">
                    {scheme.eligibility.map((item, i) => (
                      <div key={i} className="flex items-start gap-3">
                        <CheckCircle2
                          size={16}
                          className="mt-1 shrink-0 text-zinc-300"
                        />
                        <span className="text-[15px] leading-7 text-zinc-300">
                          {item}
                        </span>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* VS Separator */}
              {index < schemes.length - 1 && (
                <div className="flex items-center justify-center py-1">
                  <div className="rounded-full border border-zinc-800 bg-zinc-950 px-3 py-1 text-xs font-bold text-zinc-500">
                    VS
                  </div>
                </div>
              )}
            </React.Fragment>
          ))}
        </div>

        {/* Dedicated Recommendation Block */}
        {recommendation && (
          <div className="mt-4 rounded-xl border border-zinc-800 bg-zinc-900 p-5 shadow-sm">
            <div className="mb-3 flex items-center gap-2">
              <Trophy size={16} className="text-zinc-300" />
              <h4 className="text-[15px] font-semibold text-zinc-100">
                AI Recommendation
              </h4>
            </div>
            <p className="whitespace-pre-wrap text-[15px] leading-7 text-zinc-300">
              {recommendation}
            </p>
          </div>
        )}
      </div>
    </div>
  );
}