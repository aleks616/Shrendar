import {defineConfig} from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite'
export default defineConfig({
    root: '.',
    plugins: [
        tailwindcss(),
        react(),
    ],
    build: {
        outDir: 'dist',
        emptyOutDir: true,
    },
    server: {port: 8080},
});