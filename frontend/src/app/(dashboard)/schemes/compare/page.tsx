"use client";
import { Suspense } from "react";
import { useSearchParams } from "next/navigation";
import Link from "next/link";
import { ArrowLeft, ExternalLink } from "lucide-react";
import { useSchemes } from "@/features/schemes/hooks/useSchemes";
export default function ComparisonPage() {
  return (
    <Suspense fallback={<p>Loading comparison…</p>}>
      <Comparison />
    </Suspense>
  );
}
function Comparison() {
  const params = useSearchParams();
  const query = useSchemes();
  const ids = [...new Set((params.get("ids") ?? "").split(","))].slice(0, 3);
  const schemes = ids.flatMap(
    (id) => query.data?.find((s) => s.id === id) ?? [],
  );
  return (
    <div className="space-y-7">
      <Link href="/schemes" className="text-link">
        <ArrowLeft size={16} />
        Back to schemes
      </Link>
      <div>
        <h1 className="editorial text-4xl">A clearer comparison.</h1>
        <p className="mt-3 text-sm text-muted-foreground">
          Compare recorded benefits and requirements. Confirm the latest details
          with each official source.
        </p>
      </div>
      {query.isPending ? (
        <p role="status">Loading schemes…</p>
      ) : query.isError ? (
        <div role="alert">
          Couldn’t load comparison.{" "}
          <button className="text-link" onClick={() => query.refetch()}>
            Retry
          </button>
        </div>
      ) : schemes.length < 2 ? (
        <p>Select at least two published schemes from the catalogue.</p>
      ) : (
        <div className="overflow-x-auto rounded-xl border bg-white">
          <table className="w-full min-w-[660px] border-collapse text-left text-sm">
            <caption className="sr-only">
              Comparison of {schemes.map((s) => s.name).join(", ")}
            </caption>
            <thead>
              <tr>
                <th className="w-36 border-b bg-muted/40 p-5">Scheme</th>
                {schemes.map((s) => (
                  <th key={s.id} className="min-w-56 border-b p-5 align-top">
                    <Link
                      className="text-primary hover:underline"
                      href={`/schemes/${s.id}`}
                    >
                      {s.name}
                    </Link>
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {[
                {
                  label: "Jurisdiction",
                  values: schemes.map((s) => [s.state]),
                },
                {
                  label: "Department",
                  values: schemes.map((s) => [s.department]),
                },
                { label: "Benefits", values: schemes.map((s) => s.benefits) },
                {
                  label: "Documents",
                  values: schemes.map((s) => s.requiredDocuments),
                },
                {
                  label: "Verified",
                  values: schemes.map((s) => [
                    s.lastVerifiedAt
                      ? new Date(s.lastVerifiedAt).toLocaleDateString("en-IN")
                      : "Not recorded",
                  ]),
                },
              ].map((row) => (
                <tr key={row.label}>
                  <th
                    scope="row"
                    className="border-b bg-muted/40 p-5 align-top font-medium"
                  >
                    {row.label}
                  </th>
                  {row.values.map((values, i) => (
                    <td
                      key={schemes[i].id}
                      className="border-b p-5 align-top text-muted-foreground"
                    >
                      <ul className="space-y-3">
                        {(values.length ? values : ["Not recorded"]).map(
                          (v) => (
                            <li key={v}>{v}</li>
                          ),
                        )}
                      </ul>
                    </td>
                  ))}
                </tr>
              ))}
              <tr>
                <th scope="row" className="bg-muted/40 p-5">
                  Next step
                </th>
                {schemes.map((s) => (
                  <td key={s.id} className="p-5">
                    <Link href={`/schemes/${s.id}`} className="text-link">
                      Check my eligibility
                    </Link>
                    {s.officialUrl && (
                      <a
                        href={s.officialUrl}
                        target="_blank"
                        rel="noreferrer"
                        className="mt-3 flex items-center gap-2 text-xs text-muted-foreground underline"
                      >
                        Official source <ExternalLink size={12} />
                      </a>
                    )}
                  </td>
                ))}
              </tr>
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
