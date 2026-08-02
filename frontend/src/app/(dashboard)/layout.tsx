import { Navbar } from "@/components/layout/Navbar";
import { Footer } from "@/components/layout/Footer";

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <>
      <Navbar />

      <main
        id="main-content"
        className="min-h-[calc(100vh-8rem)]"
      >
        {children}
      </main>

      <Footer />
    </>
  );
}