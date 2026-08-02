import { PageContainer } from "@/components/layout/PageContainer";

export default function AuthLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <PageContainer>
      <div className="mx-auto flex min-h-[80vh] max-w-md items-center justify-center">
        <div className="w-full rounded-lg border bg-white p-8 shadow-sm">
          {children}
        </div>
      </div>
    </PageContainer>
  );
}