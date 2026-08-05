"use client";

import { LucideIcon } from "lucide-react";

interface Props {
    title: string;
    subtitle?: string;
    icon: LucideIcon;
    color?: string;
}

export default function CardHeader({
    title,
    subtitle,
    icon: Icon,
    color = "blue",
}: Props) {
    return (
        <div className="flex items-center gap-4 border-b border-zinc-200 dark:border-zinc-800 pb-4">

            <div
                className={`
                rounded-2xl
                p-3
                bg-${color}-100
                dark:bg-${color}-950
            `}
            >
                <Icon
                    className={`text-${color}-600`}
                    size={20}
                />
            </div>

            <div>

                <h3 className="font-semibold text-lg">

                    {title}

                </h3>

                {subtitle && (

                    <p className="text-sm text-zinc-500">

                        {subtitle}

                    </p>

                )}

            </div>

        </div>
    );
}