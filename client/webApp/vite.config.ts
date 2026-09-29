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
    server: {
        port: 8080,
        proxy: {
            '/api': {
                target: 'https://chairs-diet-carries-voting.trycloudflare.com',
                //target: 'https://shrendar.shares.zrok.io',
                changeOrigin: true,
            },
        },
        allowedHosts: ['shrendarclient.shares.zrok.io',/*'unable-insulation-mae-bring.trycloudflare.com'*/],
    },
});