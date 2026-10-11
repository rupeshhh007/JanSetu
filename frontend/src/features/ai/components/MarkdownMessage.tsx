import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
export default function MarkdownMessage({ content }: { content: string }) {
  return (
    <article className="break-words text-sm leading-7 [&_h2]:mb-3 [&_h2]:text-lg [&_h3]:mb-2 [&_h3]:mt-5 [&_h3]:text-base [&_p]:my-3 [&_ul]:my-3 [&_ul]:list-disc [&_ul]:pl-5 [&_ol]:list-decimal [&_ol]:pl-5 [&_li]:my-1 [&_a]:text-primary [&_a]:underline [&_table]:w-full [&_td]:border [&_td]:p-2 [&_th]:border [&_th]:p-2">
      <ReactMarkdown remarkPlugins={[remarkGfm]}>{content}</ReactMarkdown>
    </article>
  );
}
