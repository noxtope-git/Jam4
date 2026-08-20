import app from './app.js';

const PORT = process.env.PORT || 4000;

app.listen(PORT, () => {
  console.log(`🚀 Jam! backend corriendo en http://localhost:${PORT}`);
});
