/*TRUNCATE TABLE candidacies, candidate_profiles RESTART IDENTITY CASCADE;


-- Seed für candidate_profiles
INSERT INTO candidate_profiles (user_id, firstname, lastname, faculty_id, about_me)
VALUES
  (1, 'Alex', 'Muster', 1, 'Ich setze mich für Digitalisierung ein.'),
  (2, 'Sam', 'Beispiel', 2, 'Mehr Transparenz und Service.');

-- Seed für candidacies
INSERT INTO candidacies (user_id, committee_id, list_id)
VALUES
  (1, 'FSR', 101),
  (1, 'AS',  301),
  (2, 'FBR', 201);
*/