import ReactMarkdown from "react-markdown";

import remarkGfm from "remark-gfm";

import rehypeHighlight from "rehype-highlight";

interface Props {

  content: string;

}

export default function MarkdownMessage({

  content,

}: Props) {

  return (

    <article
      className="
      prose
      prose-zinc
      dark:prose-invert
      max-w-none
      prose-headings:font-semibold
      prose-p:leading-7
      prose-li:leading-7
    "
    >

      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        rehypePlugins={[rehypeHighlight]}
      >
        {content}
      </ReactMarkdown>

    </article>

  );

}