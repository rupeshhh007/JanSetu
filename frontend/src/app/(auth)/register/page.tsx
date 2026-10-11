import { RegisterForm } from "@/features/auth/components/RegisterForm";

export default function RegisterPage() {
  return (
    <div>
      <div className="mb-8">
        <p className="noticeboard-kicker text-primary">Start here</p>
        <h1 className="mt-2 text-4xl leading-none">Create your desk.</h1>
        <p className="mt-3 text-text-muted">
          Use an email you can access. You will build your profile next.
        </p>
      </div>

      <RegisterForm />
    </div>
  );
}
