"use client";

import { ArrowRight } from "lucide-react";

interface Props {

    children: React.ReactNode;

    onClick?: () => void;

}

export default function ActionButton({

    children,

    onClick,

}: Props) {

    return (

        <button
            onClick={onClick}
            className="
            inline-flex
            items-center
            gap-2
            rounded-xl
            bg-blue-600
            px-5
            py-2.5
            text-sm
            font-medium
            text-white
            transition
            hover:bg-blue-700
        "
        >

            {children}

            <ArrowRight size={16} />

        </button>

    );

}