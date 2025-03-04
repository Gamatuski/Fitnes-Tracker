const express = require('express');
const mongoose = require('mongoose');
const bodyParser = require('body-parser');
const cors = require('cors');
const bcrypt = require('bcrypt'); // Добавляем bcrypt для хеширования паролей

const app = express();
const PORT = 3000;

app.use(bodyParser.json());
app.use(cors());

mongoose.connect('mongodb://localhost:27017/fitness_db', {
    useNewUrlParser: true,
    useUnifiedTopology: true,
}).then(() => {
    console.log('Connected to MongoDB');
}).catch((err) => {
    console.error('Failed to connect to MongoDB', err);
    process.exit(1); // Завершаем процесс при ошибке подключения
});

const userSchema = new mongoose.Schema({
    email: { type: String, required: true, unique: true },
    password: { type: String, required: true },
    height: { type: Number, required: true },
    weight: { type: Number, required: true },
    steps: { type: [Number], default: [0, 0, 0, 0, 0, 0, 0] },
});

const User = mongoose.model('User', userSchema);

const workoutSchema = new mongoose.Schema({
    userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    action: { type: String, required: true },
    distance: { type: Number, required: true },
    duration: { type: Number, required: true },
    calories: { type: Number, required: true },
    steps: { type: Number, required: true },
    date: { type: Date, default: Date.now },
});

const Workout = mongoose.model('Workout', workoutSchema);

const activitySchema = new mongoose.Schema({
    activityName: {
        type: String,
        required: true,
        unique: true, // Опционально: если имена активностей должны быть уникальными
        trim: true // Опционально: удаление пробелов в начале и конце имени
    },
    MET: {
        type: Number,
        required: true,
        min: 0 // Опционально: MET не может быть отрицательным
    }
});

const Activity = mongoose.model('Activity', activitySchema, 'activities'); // 'activities' - имя коллекции в MongoDB

module.exports = Activity;

app.get('/', (req, res) => {
    res.json({ message: 'Сервер Fitness Tracker работает!' });
});

app.post('/auth/register', async (req, res) => {
    const { email, password, height, weight } = req.body;

    try {
        const existingUser = await User.findOne({ email });
        if (existingUser) {
            return res.status(400).json({ success: false, message: 'Пользователь уже существует' });
        }

        const hashedPassword = await bcrypt.hash(password, 10); // Хешируем пароль
        const user = new User({ email, password: hashedPassword, height, weight });
        await user.save();

        res.status(201).json({ success: true, message: 'Пользователь зарегистрирован' });
    } catch (err) {
        console.error('Error during registration:', err);
        res.status(500).json({ success: false, message: 'Ошибка сервера' });
    }
});

app.post('/auth/login', async (req, res) => {
    const { email, password } = req.body;

    try {
        const user = await User.findOne({ email });
        if (!user) {
            return res.status(400).json({ success: false, message: 'Пользователь не найден' });
        }

        const passwordMatch = await bcrypt.compare(password, user.password); // Сравниваем хеши паролей
        if (!passwordMatch) {
            return res.status(400).json({ success: false, message: 'Неверный пароль' });
        }

        res.status(200).json({ success: true, message: 'Вход выполнен', user });
    } catch (err) {
        console.error('Error during login:', err);
        res.status(500).json({ success: false, message: 'Ошибка сервера' });
    }
});

app.get('/steps/:userId', async (req, res) => {
    const { userId } = req.params;

    try {
        const user = await User.findById(userId);
        if (!user) {
            return res.status(404).json({ success: false, message: 'Пользователь не найден' });
        }

        res.status(200).json({ success: true, steps: user.steps });
    } catch (err) {
        console.error('Error getting steps:', err);
        res.status(500).json({ success: false, message: 'Ошибка сервера' });
    }
});

app.post('/steps/:userId', async (req, res) => {
    const { userId } = req.params;
    const { steps, dayIndex } = req.body;

    try {
        const user = await User.findById(userId);
        if (!user) {
            return res.status(404).json({ success: false, message: 'Пользователь не найден' });
        }

        user.steps[dayIndex] = steps;
        await user.save();

        res.status(200).json({ success: true, steps: user.steps });
    } catch (err) {
        console.error('Error updating steps:', err);
        res.status(500).json({ success: false, message: 'Ошибка сервера' });
    }
});

app.post('/workouts', async (req, res) => {
    const { userId, action, distance, duration, calories, steps } = req.body;

    try {
        const workout = new Workout({ userId, action, distance, duration, calories, steps });
        await workout.save();

        res.status(201).json({ success: true, message: 'Тренировка добавлена', workout });
    } catch (err) {
        console.error('Error creating workout:', err);
        res.status(500).json({ success: false, message: 'Ошибка сервера' });
    }
});

app.get('/workouts/:userId', async (req, res) => {
    const { userId } = req.params;

    try {
        const workouts = await Workout.find({ userId });
        res.status(200).json({ success: true, workouts });
    } catch (err) {
        console.error('Error getting workouts:', err);
        res.status(500).json({ success: false, message: 'Ошибка сервера' });
    }
});

// Функция для получения MET по имени активности
async function getMETValueForActivity(activityName) {
    try {
        const activity = await Activity.findOne({ activityName: activityName }); // Поиск активности по имени
        if (activity) {
            return activity.MET; // Возвращаем значение MET, если активность найдена
        } else {
            return null; // Возвращаем null, если активность не найдена
        }
    } catch (error) {
        console.error("Ошибка при запросе MET для активности:", activityName, error);
        return null; // В случае ошибки также возвращаем null
    }
}

app.listen(PORT, () => {
    console.log(`Сервер запущен на http://localhost:${PORT}`);
});