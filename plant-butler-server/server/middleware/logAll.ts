// server/middleware/logAll.ts
export default defineEventHandler((event) => {
    const { method, url, headers } = event.node.req;
    console.log(`[Incoming Request] ${method} ${url}`);
});