import type { ApplicationResponse } from "../types";
import { ApplicationCard } from "./ApplicationCard";

interface Props {
  applications: ApplicationResponse[];
}

export function ApplicationGrid({ applications }: Props) {
  return (
    <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
      {applications.map((application) => (
        <ApplicationCard
          key={application.applicationId}
          application={application}
        />
      ))}
    </div>
  );
}