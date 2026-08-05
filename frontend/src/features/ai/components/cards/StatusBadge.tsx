"use client";

import {
    CheckCircle2,
    Clock3,
    AlertCircle,
} from "lucide-react";

interface Props {

    status: string;

}

export default function StatusBadge({

    status,

}: Props) {

    switch (status) {

        case "APPROVED":

            return (

                <div className="inline-flex items-center gap-2 rounded-full bg-green-100 px-3 py-1 text-green-700">

                    <CheckCircle2 size={16} />

                    Approved

                </div>

            );

        case "SUBMITTED":

            return (

                <div className="inline-flex items-center gap-2 rounded-full bg-yellow-100 px-3 py-1 text-yellow-700">

                    <Clock3 size={16} />

                    Under Review

                </div>

            );

        case "REJECTED":

            return (

                <div className="inline-flex items-center gap-2 rounded-full bg-red-100 px-3 py-1 text-red-700">

                    <AlertCircle size={16} />

                    Rejected

                </div>

            );

        default:

            return (

                <div className="inline-flex rounded-full bg-zinc-100 px-3 py-1">

                    {status}

                </div>

            );

    }

}