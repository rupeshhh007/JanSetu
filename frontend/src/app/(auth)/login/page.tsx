import { LoginForm } from "@/features/auth/components/LoginForm";

export default function LoginPage() {
  return (
    <div>
      <div className="mb-8">
        <p className="noticeboard-kicker text-primary">Return to your desk</p>
        <h1 className="mt-2 text-4xl leading-none">Welcome back.</h1>
        <p className="mt-3 text-text-muted">
          Sign in to continue your scheme search and applications.
        </p>
      </div>

      <LoginForm />
    </div>
  );
}
