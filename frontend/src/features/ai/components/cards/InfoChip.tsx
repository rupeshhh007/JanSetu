"use client";

import { LucideIcon } from "lucide-react";

interface Props {

    icon: LucideIcon;

    children: React.ReactNode;

}

export default function InfoChip({

    icon: Icon,

    children,

}: Props) {

    return (

        <div
            className="
            inline-flex
            items-center
            gap-2
            rounded-full
            bg-zinc-100
            dark:bg-zinc-800
            px-3
            py-1
            text-sm
        "
        >

            <Icon size={14} />

            {children}

        </div>

    );

}