import Link from "next/link";

export const metadata = {
  title: "Privacy notice — JanSetu",
  description: "Understand what JanSetu stores and the limitations of the public pilot.",
};

export default function PrivacyNotice() {
  return (
    <main className="mx-auto max-w-3xl space-y-8 px-5 py-12">
      <Link href="/" className="text-sm underline">← JanSetu</Link>
      <h1 className="text-4xl font-semibold">Privacy notice</h1>
      <p className="text-sm leading-7">
        JanSetu is an independently built pilot for discovering public benefit schemes and
        preparing an application. It is not operated by the Government of India or a state government.
        It does not transmit your application to any government portal.
      </p>
      <section>
        <h2 className="text-xl font-semibold">Information used</h2>
        <p className="mt-2 text-sm leading-7">
          If you create an account, the service stores your email address and an encrypted
          password verifier, profile attributes you provide, declared facts and consent settings,
          your saved schemes, recorded recommendations or policy assessments, locally reported
          application progress, and in-app notifications. This information is used to provide
          scheme discovery and preparation features. Do not upload identity documents.
        </p>
      </section>
      <section>
        <h2 className="text-xl font-semibold">Hosting and AI</h2>
        <p className="mt-2 text-sm leading-7">
          The public pilot uses Vercel for the website, Render for the API and Neon
          PostgreSQL for storage. Server and platform operators may process information
          needed to deliver the service. Optional external AI provider calls are disabled
          for the public pilot; local deterministic explanations remain available.
        </p>
      </section>
      <section>
        <h2 className="text-xl font-semibold">Your controls</h2>
        <p className="mt-2 text-sm leading-7">
          You can review your profile and download a JSON copy of your account records
          from Privacy &amp; data when signed in. Automated account erasure and fixed
          retention schedules are not yet available in this pilot. Avoid entering sensitive
          real-world information until those safeguards are implemented and published.
        </p>
      </section>
      <section>
        <h2 className="text-xl font-semibold">Questions and vulnerability reports</h2>
        <p className="mt-2 text-sm leading-7">
          Contact the JanSetu repository maintainer privately using their GitHub profile.
          Do not share identity documents, credentials or vulnerable account details in public issues.
        </p>
        <a className="mt-3 inline-block text-sm underline" href="https://github.com/rupeshhh007/JanSetu">
          JanSetu repository
        </a>
      </section>
    </main>
  );
}
